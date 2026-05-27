import { defineConfig, globalIgnores } from "eslint/config";
import nextVitals from "eslint-config-next/core-web-vitals";
import nextTs from "eslint-config-next/typescript";

const eslintConfig = defineConfig([
  ...nextVitals,
  ...nextTs,
  globalIgnores([".next/**", "out/**", "build/**", "next-env.d.ts"]),

  // 프로젝트 헌법 강제 룰
  {
    rules: {
      // any 사용 금지 (헌법 13번 12항). unknown으로 받고 Zod/타입 가드로 좁힌다.
      "@typescript-eslint/no-explicit-any": "error",

      // import 순서: 외부 → @/* → 같은 폴더 상대 (헌법 6번)
      "import/order": [
        "error",
        {
          groups: [
            "builtin",
            "external",
            "internal",
            ["parent", "sibling", "index"],
          ],
          pathGroups: [
            { pattern: "@/**", group: "internal", position: "before" },
          ],
          pathGroupsExcludedImportTypes: ["builtin"],
          "newlines-between": "always",
          alphabetize: { order: "asc", caseInsensitive: true },
        },
      ],

      // default export 금지 — 라우팅 파일은 아래 overrides에서 풀어준다.
      "import/no-default-export": "error",

      // 상대경로 두 단계 이상 금지 — @/* alias 강제
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              group: ["../../*"],
              message:
                "두 단계 이상의 상대경로 import 금지. @/* alias 사용 (헌법 6번 항목).",
            },
          ],
        },
      ],
    },
  },

  // Next 라우팅 파일과 설정 파일은 default export 단독만 허용 (헌법 6번 보강된 예외)
  {
    files: [
      "src/app/**/page.tsx",
      "src/app/**/layout.tsx",
      "src/app/**/error.tsx",
      "src/app/**/not-found.tsx",
      "src/app/**/loading.tsx",
      "src/app/**/template.tsx",
      "src/app/**/default.tsx",
      "src/app/**/global-error.tsx",
      "next.config.ts",
      "eslint.config.mjs",
    ],
    rules: {
      "import/no-default-export": "off",
    },
  },
]);

export default eslintConfig;
