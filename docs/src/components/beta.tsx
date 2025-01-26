import { MDXProvider } from "@mdx-js/react";
import { useMDXComponents } from "nextra/mdx";

export function Beta({ children }: { children: string }) {
  const components = useMDXComponents();
  const H1 = components.h4 || "h4";
  const generateId = (text: string) => text.toLowerCase().replace(/\s+/g, "-");
  const id = generateId(children);
  return (
    <MDXProvider components={components}>
      <div className="flex items-center">
        <H1 id={id}>{children}</H1>
        <div
          style={{ marginTop: 35, backgroundColor: "#95D1EA" }}
          className="px-2 py-1 rounded-md text-black text-sm font-medium ml-2"
          role="alert"
        >
          <p className="font-bold">Beta</p>
        </div>
        {/* <a
          href={`#${id}`}
          className="_not-prose subheading-anchor"
          aria-label="Permalink for this section"
        /> */}
      </div>
    </MDXProvider>
  );
}
