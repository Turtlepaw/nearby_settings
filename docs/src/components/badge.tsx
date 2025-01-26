/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { useEffect, useState } from "react";
import { useTheme } from "nextra-theme-docs";

interface BadgeProps {
  url?: string;
  leadingText?: string;
  supportingText?: string;
  loading?: boolean;
  icon?: string;
}

export function Badge({
  url,
  leadingText,
  supportingText,
  loading,
  icon,
}: BadgeProps) {
  const { resolvedTheme } = useTheme();
  const isDark = resolvedTheme == "dark";

  const styles = {
    background: isDark ? "#FFFFFF10" : "#00000010",
    borderColor: isDark ? "#FFFFFF15" : "#00000015",
    hoverBackground: isDark ? "#FFFFFF20" : "#00000020",
    textColor: isDark ? "#FFFFFF" : "#000000",
    secondaryTextColor: isDark ? "#FFFFFF99" : "#00000099",
  };

  return (
    <div style={{ display: "flex", justifyContent: "center", paddingTop: 15 }}>
      <a
        href={url}
        target="_blank"
        rel="noopener noreferrer"
        style={{
          display: "inline-block",
          textDecoration: "none",
          color: styles.textColor,
        }}
      >
        <div
          style={{
            display: "flex",
            flexDirection: "row",
            alignItems: "center",
            background: styles.background,
            borderColor: styles.borderColor,
            borderWidth: 1,
            borderRadius: 8,
            padding: "4px 10px",
            maxWidth: "fit-content",
            transition: "all 0.2s ease",
            cursor: "pointer",
          }}
          onMouseOver={(e) => {
            if (loading) return;
            e.currentTarget.style.background = styles.hoverBackground;
            e.currentTarget.style.transform = "translateY(-1px)";
          }}
          onMouseOut={(e) => {
            if (loading) return;
            e.currentTarget.style.background = styles.background;
            e.currentTarget.style.transform = "translateY(0)";
          }}
          onMouseDown={(e) => {
            if (loading) return;
            e.currentTarget.style.background = styles.hoverBackground;
            e.currentTarget.style.transform = "translateY(1px)";
          }}
        >
          {loading && <div>Loading...</div>}
          {!loading && (
            <div style={{ display: "flex" }}>
              {icon && (
                <div style={{ display: "flex", alignItems: "center" }}>
                  <span
                    className="material-symbols-rounded"
                    style={{ marginRight: 6, fontSize: 20 }} // Adjust the fontSize value as needed
                  >
                    {icon}
                  </span>
                </div>
              )}
              <p style={{ marginRight: "10px", margin: "0" }}>{leadingText}</p>
              {supportingText && <div style={{ paddingRight: 5 }} />}
              {supportingText && (
                <p
                  style={{
                    marginRight: "10px",
                    margin: "0",
                    color: styles.secondaryTextColor,
                  }}
                >
                  {supportingText}
                </p>
              )}
            </div>
          )}
        </div>
      </a>
    </div>
  );
}

export function LatestRelease() {
  const [release, setRelease] = useState<any | null>(null);
  const [error, setError] = useState(null);

  //@ts-expect-error intl probably doesn't need to know all formats
  const units: Record<Intl.RelativeTimeFormatUnit, number> = {
    year: 24 * 60 * 60 * 1000 * 365,
    month: (24 * 60 * 60 * 1000 * 365) / 12,
    day: 24 * 60 * 60 * 1000,
    hour: 60 * 60 * 1000,
    minute: 60 * 1000,
    second: 1000,
  };

  const rtf = new Intl.RelativeTimeFormat("en", { numeric: "auto" });

  function getRelativeTime(d1: Date, d2 = new Date()) {
    const elapsed = d1.getTime() - d2.getTime();

    // "Math.abs" accounts for both "past" & "future" scenarios
    for (const [u, v] of Object.entries(units))
      if (Math.abs(elapsed) > v || u == "second")
        return rtf.format(
          Math.round(elapsed / v),
          u as Intl.RelativeTimeFormatUnit
        );
  }

  useEffect(() => {
    const fetchLatestRelease = async () => {
      try {
        const response = await fetch(
          "https://api.github.com/repos/turtlepaw/clockwork/releases/latest"
        );
        if (!response.ok) {
          throw new Error(`Error: ${response.status} ${response.statusText}`);
        }
        const data = await response.json();
        setRelease(data);
      } catch (err: any) {
        setError(err.message);
      }
    };

    fetchLatestRelease();
  }, []);

  if (error) {
    return <div>Error fetching release: {error}</div>;
  }

  return (
    <Badge
      url={release?.html_url}
      leadingText={`v${release?.tag_name}`}
      supportingText={`published ${
        release ? getRelativeTime(new Date(release.published_at)) : undefined
      }`}
      loading={!release}
      icon="deployed_code_history"
    />
  );
}

export function StarBadge() {
  const [data, setData] = useState<{
    html_url?: string;
    stargazers_count?: number;
  } | null>(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchStars = async () => {
      try {
        const response = await fetch(
          "https://api.github.com/repos/turtlepaw/clockwork"
        );
        if (!response.ok) {
          throw new Error(`Error: ${response.status} ${response.statusText}`);
        }
        const data = await response.json();
        setData(data);
      } catch (err: any) {
        setError(err.message);
      }
    };

    fetchStars();
  }, []);

  if (error) {
    return <div>Error fetching release: {error}</div>;
  }

  const formattedStargazersCount = data?.stargazers_count?.toLocaleString();

  return (
    <Badge
      url={data?.html_url}
      leadingText={"Star us on GitHub"}
      supportingText={
        typeof data?.stargazers_count == "number"
          ? `${formattedStargazersCount} star${
              data.stargazers_count > 1 ? "s" : ""
            }`
          : undefined
      }
      //loading={!data}
      icon="star"
    />
  );
}
