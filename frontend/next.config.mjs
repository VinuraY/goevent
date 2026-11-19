/**
 * Event and profile images are served by the backend under the API origin.
 * The Next.js image optimizer only fetches hosts listed here, so the API host
 * must be allowed. Resolve it the same way as src/api/routes.ts does.
 */
const apiImagePattern = () => {
  const configured =
    process.env.API_URL ||
    process.env.NEXT_PUBLIC_API_URL ||
    process.env.NEXT_PUBLIC_API_BASE_URL ||
    '';
  try {
    const { protocol, hostname, port } = new URL(configured);
    return {
      protocol: protocol.replace(':', ''),
      hostname,
      ...(port ? { port } : {}),
    };
  } catch {
    // Relative API prefix (same origin): no remote host to allow.
    return null;
  }
};

const remotePatterns = [
  {
    protocol: 'https',
    hostname: 'images.unsplash.com',
  },
  apiImagePattern(),
].filter(Boolean);

/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  env: {
    API_URL: process.env.API_URL || process.env.NEXT_PUBLIC_API_BASE_URL || '/goevent/api',
  },
  images: {
    remotePatterns,
  },
  async rewrites() {
    return [
      {
        source: '/goevent/api/:path*',
        destination: `${process.env.BACKEND_URL || 'http://localhost:8080'}/goevent/api/:path*`,
      },
      {
        source: '/api/v1/:path*',
        destination: `${process.env.BACKEND_URL || 'http://localhost:8080'}/goevent/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
