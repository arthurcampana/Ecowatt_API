import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Dev server na porta 5500 para casar com o CORS do backend
// (SecurityConfig permite http://localhost:5500 e http://127.0.0.1:5500)
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5500,
    host: true,
  },
});
