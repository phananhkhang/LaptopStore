import { defineConfig, loadEnv, mergeConfig } from "vite";
import baseConfig from "./vite.config";

// Keep the shared development config intact; use Spring Boot's default port for this flow.
export default defineConfig(async (configEnv) => {
  const base =
    typeof baseConfig === "function"
      ? await baseConfig(configEnv)
      : await baseConfig;
  const env = loadEnv(configEnv.mode, process.cwd(), "");
  return mergeConfig(base, {
    server: {
      proxy: {
        "/api": {
          target: env.API_PROXY_TARGET || "http://127.0.0.1:8081",
          changeOrigin: true,
        },
        "/uploads": {
          target: env.API_PROXY_TARGET || "http://127.0.0.1:8081",
          changeOrigin: true,
        },
      },
    },
  });
});
