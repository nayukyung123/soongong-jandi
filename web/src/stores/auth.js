import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

/** localStorage 키 — `src/api/http.js` 와 동일해야 합니다. */
export const AUTH_TOKEN_KEY = 'api_jwt_token';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(AUTH_TOKEN_KEY) || null);

  function setToken(value) {
    token.value = value;
    if (value) {
      localStorage.setItem(AUTH_TOKEN_KEY, value);
    } else {
      localStorage.removeItem(AUTH_TOKEN_KEY);
    }
  }

  function clearToken() {
    setToken(null);
  }

  const isAuthenticated = computed(() => Boolean(token.value));

  return {
    token,
    setToken,
    clearToken,
    isAuthenticated,
  };
});
