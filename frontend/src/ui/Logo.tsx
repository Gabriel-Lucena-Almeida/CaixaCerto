export function Logo({ className = 'w-11 h-11' }: { className?: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 44 44"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      role="img"
      aria-label="CaixaCerto"
    >
      <rect width="44" height="44" rx="12" fill="#14263B" />
      <path
        d="M12 23.5L19 30.5L32 15"
        stroke="#5FD3A5"
        strokeWidth="4"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}