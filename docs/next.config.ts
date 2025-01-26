import path from "node:path";
import nextra from "nextra";

const withNextra = nextra({
  theme: "nextra-theme-docs",
  themeConfig: "./theme.config.tsx",
  latex: true,
  search: {
    codeblocks: false,
  },
  defaultShowCopyCode: true,
});

export default withNextra({
  reactStrictMode: true,
  // experimental: {
  //   optimizePackageImports: ["@components/icons", "framer-motion"],
  // },
});
