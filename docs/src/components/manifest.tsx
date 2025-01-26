/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { useEffect, useState } from "react";
import yaml from "js-yaml";
import { Table, Tr, Th, Td, Code } from "nextra/components";

export function Manifest() {
  const [manifest, setManifest] = useState<Record<
    string,
    { url: string }
  > | null>(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchManifestAndLoad = async () => {
      try {
        const response = await fetch(
          "https://raw.githubusercontent.com/Turtlepaw/clockwork/refs/heads/main/manifest.yml"
        );
        if (!response.ok) {
          throw new Error(`Error: ${response.status} ${response.statusText}`);
        }
        const data = yaml.load(await response.text());
        setManifest(data as any);
      } catch (err: any) {
        setError(err.message);
      }
    };

    fetchManifestAndLoad();
  }, []);

  if (error) {
    return <div>Error fetching release: {error}</div>;
  }

  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        paddingTop: 15,
      }}
    >
      {!manifest && <div>Loading manifest...</div>}
      {manifest && (
        <Table style={{ width: "100%", borderCollapse: "collapse" }}>
          <thead>
            <Tr>
              <Th align="left">Package</Th>
              <Th align="left">URL</Th>
            </Tr>
          </thead>
          <tbody>
            {Object.entries(manifest).map(([name, { url }]) => (
              <Tr key={name}>
                <Td>
                  <Code>{name}</Code>
                </Td>
                <Td>
                  <a
                    href={url}
                    style={{ color: "#3B82F6", textDecoration: "none" }}
                  >
                    {url.replace(".git", "")}
                  </a>
                </Td>
              </Tr>
            ))}
          </tbody>
        </Table>
      )}
    </div>
  );
}
