import { createApp } from 'vue';
import { createPinia } from 'pinia';
import './style.css';
import App from './App.vue';
import { applyOAuthCallbackFromUrl } from './utils/oauthCallback.js';
import { useAuthStore } from './stores/auth.js';

const pinia = createPinia();
const app = createApp(App);
app.use(pinia);

applyOAuthCallbackFromUrl(useAuthStore());

app.mount('#app');
