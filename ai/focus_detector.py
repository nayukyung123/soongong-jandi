"""
순공잔디 - YOLO11 집중 감지 테스트 스크립트
==========================================
사전학습된 YOLO11 모델로 세 가지 상태를 분류합니다.

  1. 핸드폰 사용중  : 사람 O + 핸드폰 O
  2. 공부중        : 사람 O + 핸드폰 X
  3. 자리비움      : 사람 X (핸드폰 여부 무관)

사용법:
  python focus_detector.py --source <경로> [옵션]

  <경로> 예시
    이미지 1장  : --source test.jpg
    이미지 폴더 : --source ./images/
    동영상 파일 : --source test.mp4
    웹캠 실시간 : --source 0

옵션:
  --model   YOLO 모델 파일 경로 (기본: yolo11n.pt)
  --conf    검출 최소 신뢰도 0~1 (기본: 0.4)
  --iou     NMS IoU 임계값 (기본: 0.45)
  --show    결과 창 실시간 표시 (동영상/웹캠)
  --save    결과 이미지/영상 저장 (./runs/ 폴더)
  --no-label 클래스 라벨 표시 끄기

예시:
  python focus_detector.py --source ./test_images/ --save
  python focus_detector.py --source 0 --show
  python focus_detector.py --source test.mp4 --conf 0.5 --show --save
"""

import argparse
import sys
import time
from pathlib import Path
from collections import defaultdict

import cv2
import numpy as np

try:
    from ultralytics import YOLO
except ImportError:
    print("[ERROR] ultralytics 패키지가 없습니다.")
    print("  pip install ultralytics")
    sys.exit(1)

YOLO("yolo11s.pt")
# ── COCO 클래스 ID (YOLO11 기본) ─────────────────────────────
PERSON_CLASS_ID     = 0
CELL_PHONE_CLASS_ID = 67  # 'cell phone'

# ── 상태 정의 ────────────────────────────────────────────────
class FocusState:
    USING_PHONE = "📱 핸드폰 사용중"
    STUDYING    = "📚 공부중"
    AWAY        = "💤 자리비움"

STATE_COLOR = {
    FocusState.USING_PHONE : (0,   80, 255),   # 빨강 계열
    FocusState.STUDYING    : (0,  200,  60),   # 초록
    FocusState.AWAY        : (160, 160, 160),  # 회색
}


# ── 핵심 분류 함수 ────────────────────────────────────────────
def classify_state(boxes_cls: list[int]) -> str:
    """
    감지된 클래스 ID 목록을 받아 집중 상태를 반환합니다.

    Args:
        boxes_cls: 한 프레임에서 감지된 클래스 ID 리스트

    Returns:
        FocusState 문자열
    """
    has_person = PERSON_CLASS_ID     in boxes_cls
    has_phone  = CELL_PHONE_CLASS_ID in boxes_cls

    if not has_person:
        return FocusState.AWAY
    if has_phone:
        return FocusState.USING_PHONE
    return FocusState.STUDYING


# ── 프레임 시각화 ─────────────────────────────────────────────
def draw_results(frame: np.ndarray, result, state: str, args) -> np.ndarray:
    """YOLO 결과 박스 + 상태 배너를 프레임에 그립니다."""

    # 바운딩 박스 그리기
    if result.boxes is not None:
        for box in result.boxes:
            cls_id = int(box.cls[0])
            conf   = float(box.conf[0])
            x1, y1, x2, y2 = map(int, box.xyxy[0])
            label  = result.names[cls_id]

            # person → 파랑, cell phone → 주황, 나머지 → 흰색
            if cls_id == PERSON_CLASS_ID:
                color = (220, 120, 0)
            elif cls_id == CELL_PHONE_CLASS_ID:
                color = (0, 140, 255)
            else:
                color = (200, 200, 200)

            cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)

            if not args.no_label:
                tag = f"{label} {conf:.2f}"
                (tw, th), _ = cv2.getTextSize(tag, cv2.FONT_HERSHEY_SIMPLEX, 0.55, 1)
                cv2.rectangle(frame, (x1, y1 - th - 6), (x1 + tw + 4, y1), color, -1)
                cv2.putText(frame, tag, (x1 + 2, y1 - 4),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.55, (255, 255, 255), 1)

    # 상단 상태 배너
    banner_h = 52
    banner   = np.zeros((banner_h, frame.shape[1], 3), dtype=np.uint8)
    banner[:] = STATE_COLOR[state]
    cv2.putText(banner, state, (12, 36),
                cv2.FONT_HERSHEY_SIMPLEX, 1.1, (255, 255, 255), 2)
    frame = np.vstack([banner, frame])
    return frame


# ── 단일 이미지 처리 ──────────────────────────────────────────
def process_image(model: YOLO, img_path: Path, args) -> dict:
    result   = model(str(img_path), conf=args.conf, iou=args.iou, verbose=False)[0]
    cls_list = [int(c) for c in result.boxes.cls] if result.boxes else []
    state    = classify_state(cls_list)

    frame = cv2.imread(str(img_path))
    frame = draw_results(frame, result, state, args)

    if args.save:
        out_dir = Path("runs/focus_test")
        out_dir.mkdir(parents=True, exist_ok=True)
        out_path = out_dir / img_path.name
        cv2.imwrite(str(out_path), frame)

    if args.show:
        cv2.imshow(img_path.name, frame)
        cv2.waitKey(0)
        cv2.destroyAllWindows()

    # 감지 세부 정보 수집
    detections = []
    if result.boxes:
        for box in result.boxes:
            detections.append({
                "class": result.names[int(box.cls[0])],
                "conf" : round(float(box.conf[0]), 3),
            })

    return {
        "file"      : img_path.name,
        "state"     : state,
        "detections": detections,
    }


# ── 동영상 / 웹캠 처리 ────────────────────────────────────────
def process_video(model: YOLO, source, args):
    cap = cv2.VideoCapture(source)
    if not cap.isOpened():
        print(f"[ERROR] 소스를 열 수 없습니다: {source}")
        return

    w   = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    h   = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fps = cap.get(cv2.CAP_PROP_FPS) or 30

    writer = None
    if args.save:
        out_dir = Path("runs/focus_test")
        out_dir.mkdir(parents=True, exist_ok=True)
        src_name = Path(str(source)).stem if not str(source).isdigit() else "webcam"
        out_path = out_dir / f"{src_name}_result.mp4"
        fourcc   = cv2.VideoWriter_fourcc(*"mp4v")
        writer   = cv2.VideoWriter(str(out_path), fourcc, fps, (w, h + 52))
        print(f"[저장] {out_path}")

    # 집계용 카운터
    state_counter: dict[str, int] = defaultdict(int)
    frame_idx = 0
    t0 = time.time()

    print("\n[실행 중] q 또는 ESC 키로 종료\n")

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        result   = model(frame, conf=args.conf, iou=args.iou, verbose=False)[0]
        cls_list = [int(c) for c in result.boxes.cls] if result.boxes else []
        state    = classify_state(cls_list)
        state_counter[state] += 1
        frame_idx += 1

        vis = draw_results(frame.copy(), result, state, args)

        # FPS 오버레이
        elapsed = time.time() - t0
        cur_fps = frame_idx / elapsed if elapsed > 0 else 0
        cv2.putText(vis, f"FPS: {cur_fps:.1f}  Frame: {frame_idx}",
                    (vis.shape[1] - 230, vis.shape[0] - 10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.5, (200, 200, 200), 1)

        if writer:
            writer.write(vis)

        if args.show:
            cv2.imshow("순공잔디 집중감지", vis)
            key = cv2.waitKey(1) & 0xFF
            if key in (ord("q"), 27):
                break

    cap.release()
    if writer:
        writer.release()
    cv2.destroyAllWindows()

    # 결과 요약
    print_video_summary(state_counter, frame_idx, fps)


def print_video_summary(counter: dict, total: int, fps: float):
    print("\n" + "=" * 50)
    print("  집중 감지 결과 요약")
    print("=" * 50)
    for state in [FocusState.STUDYING, FocusState.USING_PHONE, FocusState.AWAY]:
        cnt  = counter.get(state, 0)
        sec  = cnt / fps if fps else 0
        pct  = cnt / total * 100 if total else 0
        print(f"  {state:<18} {cnt:>5}프레임  {sec:>6.1f}초  {pct:>5.1f}%")
    print("=" * 50)
    print(f"  총 프레임: {total}")
    print("=" * 50 + "\n")


# ── 폴더 배치 처리 ────────────────────────────────────────────
def process_folder(model: YOLO, folder: Path, args):
    exts   = {".jpg", ".jpeg", ".png", ".bmp", ".webp"}
    images = sorted(p for p in folder.iterdir() if p.suffix.lower() in exts)

    if not images:
        print(f"[WARN] 이미지가 없습니다: {folder}")
        return

    print(f"\n[폴더] {folder}  —  {len(images)}개 이미지\n")
    results = []

    for img_path in images:
        r = process_image(model, img_path, args)
        results.append(r)
        det_str = ", ".join(f"{d['class']}({d['conf']})" for d in r["detections"]) or "없음"
        print(f"  {r['file']:<30} {r['state']}   [{det_str}]")

    # 상태별 집계 출력
    print("\n" + "=" * 50)
    print("  이미지 배치 결과 요약")
    print("=" * 50)
    counter: dict[str, int] = defaultdict(int)
    for r in results:
        counter[r["state"]] += 1
    for state in [FocusState.STUDYING, FocusState.USING_PHONE, FocusState.AWAY]:
        cnt = counter.get(state, 0)
        pct = cnt / len(images) * 100
        print(f"  {state:<18} {cnt:>4}장  ({pct:.1f}%)")
    print("=" * 50 + "\n")

    if args.save:
        out_dir = Path("runs/focus_test")
        print(f"[저장 완료] {out_dir.resolve()}")


# ── CLI 진입점 ────────────────────────────────────────────────
def parse_args():
    p = argparse.ArgumentParser(
        description="순공잔디 YOLO11 집중 감지 테스트",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    p.add_argument("--source", default="0",
                   help="이미지/폴더/동영상 경로 또는 웹캠 인덱스 (기본: 0)")
    p.add_argument("--model",  default="yolo11s.pt",
                   help="YOLO 모델 파일 경로 (기본: yolo11s.pt)")
    p.add_argument("--conf",   type=float, default=0.4,
                   help="검출 최소 신뢰도 (기본: 0.4)")
    p.add_argument("--iou",    type=float, default=0.45,
                   help="NMS IoU 임계값 (기본: 0.45)")
    p.add_argument("--show",   action="store_true",
                   help="결과 창 실시간 표시")
    p.add_argument("--save",   action="store_true",
                   help="결과 이미지/영상 저장 (./runs/)")
    p.add_argument("--no-label", dest="no_label", action="store_true",
                   help="클래스 라벨 표시 끄기")
    return p.parse_args()


def main():
    args = parse_args()

    # 모델 로드
    model_path = Path(args.model)
    if not model_path.exists():
        print(f"[ERROR] 모델 파일을 찾을 수 없습니다: {model_path}")
        print("  YOLO11 모델을 다운로드하여 같은 폴더에 넣어주세요.")
        print("  https://github.com/ultralytics/assets/releases  (yolo11n.pt 권장)")
        sys.exit(1)

    print(f"[모델 로드] {model_path}")
    model = YOLO(str(model_path))
    print(f"  클래스 수: {len(model.names)}")
    print(f"  person ID: {PERSON_CLASS_ID}  →  {model.names[PERSON_CLASS_ID]}")
    print(f"  phone  ID: {CELL_PHONE_CLASS_ID}  →  {model.names[CELL_PHONE_CLASS_ID]}\n")

    source = args.source

    # 소스 타입 판별
    src_path = Path(source)

    if source.isdigit():
        # 웹캠
        print(f"[웹캠] 인덱스 {source}")
        process_video(model, int(source), args)

    elif src_path.is_dir():
        # 이미지 폴더
        process_folder(model, src_path, args)

    elif src_path.is_file():
        suffix = src_path.suffix.lower()
        img_exts   = {".jpg", ".jpeg", ".png", ".bmp", ".webp"}
        video_exts = {".mp4", ".avi", ".mov", ".mkv", ".webm"}

        if suffix in img_exts:
            r = process_image(model, src_path, args)
            print(f"\n결과: {r['state']}")
            if r["detections"]:
                for d in r["detections"]:
                    print(f"  - {d['class']}  (conf: {d['conf']})")
            else:
                print("  - 감지된 객체 없음")
            if args.save:
                print(f"[저장] runs/focus_test/{src_path.name}")

        elif suffix in video_exts:
            print(f"[동영상] {src_path}")
            process_video(model, str(src_path), args)

        else:
            print(f"[ERROR] 지원하지 않는 파일 형식: {suffix}")
            sys.exit(1)
    else:
        print(f"[ERROR] 소스를 찾을 수 없습니다: {source}")
        sys.exit(1)


if __name__ == "__main__":
    main()
