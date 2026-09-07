"""
순공잔디 집중 감지 FastAPI 서버
================================
Spring Boot ApiResponse 형태에 맞춘 공통 응답 구조 사용.

실행:
    uvicorn app:app --host 0.0.0.0 --port 8000 --reload

테스트:
    curl -X POST http://localhost:8000/api/focus/check \
         -F "file=@your_image.jpg"
"""

from enum import Enum
from typing import Any, Optional

import cv2
import numpy as np
import uvicorn
from fastapi import FastAPI, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from ultralytics import YOLO


# ── 공통 응답 모델 (ApiResponse.java 와 동일한 구조) ────────────
# 주의: Pydantic BaseModel 안에서 success 는 필드명으로 쓰이므로
#       팩토리 함수를 클래스 외부 함수로 분리합니다.
class ApiResponse(BaseModel):
    success: bool
    code: Optional[str]   # 에러 코드 (성공 시 null)
    message: str
    data: Optional[Any]   # 실제 응답 데이터


def ok(data: Any = None, message: str = "OK") -> ApiResponse:
    return ApiResponse(success=True, code=None, message=message, data=data)


def fail(code: str, message: str) -> ApiResponse:
    return ApiResponse(success=False, code=code, message=message, data=None)


# ── 에러 코드 (ErrorCode.java 에 추가될 항목들) ──────────────────
class ErrorCode:
    INVALID_IMAGE    = ("AI_001", "유효하지 않은 이미지 파일입니다.")
    MODEL_ERROR      = ("AI_002", "모델 추론 중 오류가 발생했습니다.")
    UNSUPPORTED_TYPE = ("AI_003", "지원하지 않는 파일 형식입니다.")


# ── 집중 상태 Enum ────────────────────────────────────────────
class FocusState(str, Enum):
    USING_PHONE = "USING_PHONE"   # 사람 O + 핸드폰 O
    STUDYING    = "STUDYING"      # 사람 O + 핸드폰 X
    AWAY        = "AWAY"          # 사람 X


# ── 응답 데이터 스키마 ─────────────────────────────────────────
class DetectionItem(BaseModel):
    label: str
    confidence: float

class FocusCheckData(BaseModel):
    focus_state: FocusState
    detections: list[DetectionItem]


# ── COCO 클래스 ID ────────────────────────────────────────────
PERSON_CLASS_ID     = 0
CELL_PHONE_CLASS_ID = 67

ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/png", "image/webp", "image/bmp"}

# ── 모델 로드 (서버 시작 시 1회) ───────────────────────────────
MODEL_PATH = "yolo11s.pt"
print(f"[모델 로드] {MODEL_PATH} 초기화 중...")
model = YOLO(MODEL_PATH)
print("[모델 로드] 완료 ✅")


# ── FastAPI 앱 ────────────────────────────────────────────────
app = FastAPI(
    title="순공잔디 집중 감지 API",
    description="YOLO11 기반 집중 상태 판별 (공부중 / 핸드폰 사용중 / 자리비움)",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ── 핵심 분류 함수 ────────────────────────────────────────────
def classify_state(boxes_cls: list[int]) -> FocusState:
    has_person = PERSON_CLASS_ID     in boxes_cls
    has_phone  = CELL_PHONE_CLASS_ID in boxes_cls

    if not has_person:
        return FocusState.AWAY
    if has_phone:
        return FocusState.USING_PHONE
    return FocusState.STUDYING


# ── 엔드포인트 ───────────────────────────────────────────────

@app.post(
    "/api/focus/check",
    response_model=ApiResponse,
    summary="이미지 기반 집중 상태 감지",
    description="""
이미지 파일을 업로드하면 현재 집중 상태와 감지 목록을 반환합니다.

**분류 기준**
- `STUDYING`    : 사람 O, 핸드폰 X → 공부중
- `USING_PHONE` : 사람 O, 핸드폰 O → 핸드폰 사용중
- `AWAY`        : 사람 X            → 자리비움
""",
)
async def check_focus(file: UploadFile = File(..., description="판별할 이미지 (jpg/png/webp)")):

    # 1. 파일 형식 검증
    if file.content_type not in ALLOWED_CONTENT_TYPES:
        return fail(*ErrorCode.UNSUPPORTED_TYPE)

    # 2. 이미지 디코딩
    contents = await file.read()
    nparr = np.frombuffer(contents, np.uint8)
    frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if frame is None:
        return fail(*ErrorCode.INVALID_IMAGE)

    # 3. YOLO 추론
    try:
        result   = model(frame, conf=0.4, iou=0.45, verbose=False)[0]
        cls_list = [int(c) for c in result.boxes.cls] if result.boxes else []
    except Exception as e:
        print(f"[ERROR] 모델 추론 실패: {e}")
        return fail(*ErrorCode.MODEL_ERROR)

    # 4. 상태 분류
    state = classify_state(cls_list)

    # 5. 감지 세부 정보 수집 (백엔드 협의 후 필요 시 복구)
    # detections = []
    # if result.boxes:
    #     for box in result.boxes:
    #         detections.append(DetectionItem(
    #             label      = result.names[int(box.cls[0])],
    #             confidence = round(float(box.conf[0]), 3),
    #         ))

    # 6. focus_state 만 반환 (detections 제외)
    return ok(data={"focus_state": state})


@app.get(
    "/health",
    response_model=ApiResponse,
    summary="서버 상태 확인",
)
def health_check():
    return ok(data={"model": MODEL_PATH}, message="healthy")


if __name__ == "__main__":
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)