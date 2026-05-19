package com.soongongjandi.domain.todo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TileVariantTest {

    @Test
    @DisplayName("계획이 0건이면 SOIL")
    void 계획_0건이면_SOIL() {
        assertThat(TileVariant.of(0, 0, false)).isEqualTo(TileVariant.SOIL);
    }

    @Test
    @DisplayName("미래 날짜이면 계획이 있어도 SOIL")
    void 미래_날짜이면_SOIL() {
        assertThat(TileVariant.of(3, 1, true)).isEqualTo(TileVariant.SOIL);
    }

    @Test
    @DisplayName("계획이 전부 완료이면 COMPLETE")
    void 전부_완료이면_COMPLETE() {
        assertThat(TileVariant.of(3, 3, false)).isEqualTo(TileVariant.COMPLETE);
    }

    @Test
    @DisplayName("계획이 전부 미완료이면 SPROUT")
    void 전부_미완료이면_SPROUT() {
        assertThat(TileVariant.of(3, 0, false)).isEqualTo(TileVariant.SPROUT);
    }

    @Test
    @DisplayName("계획이 일부만 완료이면 GRASS")
    void 일부만_완료이면_GRASS() {
        assertThat(TileVariant.of(3, 1, false)).isEqualTo(TileVariant.GRASS);
    }
}
