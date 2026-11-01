import React from 'react';

// Bộ icon nét 24×24 lấy từ design/figma-assets/icons.json (+ các icon build.cjs bổ sung).
const ICONS = {
  search: '<circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 4.5 4.5"/>',
  cart: '<path d="M2.5 3.5h2l2.3 11.2a2 2 0 0 0 2 1.6h8.7a2 2 0 0 0 2-1.6L21 7H5.3"/><circle cx="9" cy="20" r="1"/><circle cx="18" cy="20" r="1"/>',
  user: '<circle cx="12" cy="7.5" r="3.5"/><path d="M4.5 21v-2a7.5 7.5 0 0 1 15 0v2"/>',
  grid: '<rect x="3.5" y="3.5" width="6" height="6" rx="1.3"/><rect x="14.5" y="3.5" width="6" height="6" rx="1.3"/><rect x="3.5" y="14.5" width="6" height="6" rx="1.3"/><rect x="14.5" y="14.5" width="6" height="6" rx="1.3"/>',
  box: '<path d="m3 7 9-4 9 4v10l-9 4-9-4V7Zm0 0 9 4 9-4M12 11v10M7.5 5 17 9.2"/>',
  orders: '<path d="M8 4H5a1 1 0 0 0-1 1v15a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1V5a1 1 0 0 0-1-1h-3"/><rect x="8" y="2.5" width="8" height="4" rx="1"/><path d="M8 12h8M8 16h5"/>',
  chart: '<path d="M3.5 3v17.5H21M7.5 16v-4M12 16V8M16.5 16V5"/>',
  users: '<circle cx="9" cy="7.5" r="3.5"/><path d="M2.5 21v-2a6.5 6.5 0 0 1 13 0v2M16 4.2a3.5 3.5 0 0 1 0 6.6M18 13.5a6 6 0 0 1 3.5 5.5v2"/>',
  tag: '<path d="M3.5 3.5h8l9 9a1.5 1.5 0 0 1 0 2.1l-5.9 5.9a1.5 1.5 0 0 1-2.1 0l-9-9v-8Z"/><circle cx="8" cy="8" r="1"/>',
  settings: '<path d="m9.5 3-.6 2.3-2 .9-2.1-.7-2.5 4.3L4 11.5v2l-1.7 1.7 2.5 4.3 2.1-.7 2 .9.6 2.3h5l.6-2.3 2-.9 2.1.7 2.5-4.3-1.7-1.7v-2l1.7-1.7-2.5-4.3-2.1.7-2-.9L14.5 3h-5Z"/><circle cx="12" cy="12.5" r="3"/>',
  'arrow-right': '<path d="M4 12h16m-6-6 6 6-6 6"/>',
  'arrow-left': '<path d="M20 12H4m6-6-6 6 6 6"/>',
  'chevron-down': '<path d="m6 9 6 6 6-6"/>',
  'chevron-left': '<path d="m15 6-6 6 6 6"/>',
  'chevron-right': '<path d="m9 6 6 6-6 6"/>',
  bell: '<path d="M18 8a6 6 0 0 0-12 0c0 7-2 7-2 9h16c0-2-2-2-2-9ZM10 21h4"/>',
  check: '<path d="m5 12 4.5 4.5L19 7"/>',
  truck: '<path d="M14.5 17H9M3 17H1.5V5h13v12M14.5 9H19l3.5 4v4H20"/><circle cx="6" cy="17.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/>',
  shield: '<path d="m12 2 8 3v6c0 5-4.7 8.8-8 11-3.3-2.2-8-6-8-11V5l8-3Z"/><path d="m8 11.5 2.5 2.5 5.5-5.5"/>',
  cpu: '<rect x="5.5" y="5.5" width="13" height="13" rx="2"/><rect x="9" y="9" width="6" height="6" rx=".7"/><path d="M9 2v3.5M15 2v3.5M9 18.5V22M15 18.5V22M2 9h3.5M2 15h3.5M18.5 9H22M18.5 15H22"/>',
  sparkles: '<path d="m12 3 2.3 6.7L21 12l-6.7 2.3L12 21l-2.3-6.7L3 12l6.7-2.3L12 3ZM4 2v4M2 4h4M20 18v4M18 20h4"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  minus: '<path d="M5 12h14"/>',
  filter: '<path d="M3 6h18M3 12h18M3 18h18"/><circle cx="8" cy="6" r="2" fill="white"/><circle cx="16" cy="12" r="2" fill="white"/><circle cx="10" cy="18" r="2" fill="white"/>',
  dots: '<circle cx="5" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/>',
  calendar: '<rect x="3.5" y="5" width="17" height="16" rx="2"/><path d="M7.5 2v6M16.5 2v6M3.5 10h17M8 14h3M14 14h2M8 17h3"/>',
  wallet: '<path d="M20.5 8V6a2 2 0 0 0-2-2h-13a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h13a2 2 0 0 0 2-2v-2M3.5 6a2 2 0 0 0 2 2h15v8h-5a4 4 0 0 1 0-8"/><circle cx="16" cy="12" r=".8"/>',
  logout: '<path d="M10 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h5M9 12h12m-4-4 4 4-4 4"/>',
  edit: '<path d="m15 4 5 5M4 20l5-1L21 7a1.4 1.4 0 0 0 0-2l-2-2a1.4 1.4 0 0 0-2 0L5 15l-1 5ZM4 20h16"/>',
  headset: '<path d="M4 13V10a8 8 0 0 1 16 0v7a4 4 0 0 1-4 4h-3"/><rect x="2" y="11" width="5" height="7" rx="2"/><rect x="17" y="11" width="5" height="7" rx="2"/><path d="M10 21h3"/>',
  lock: '<rect x="5" y="10" width="14" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3M12 14v3"/>',
  clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
  alert: '<path d="m12 3 10 18H2L12 3Z"/><path d="M12 9v5m0 3h.01"/>',
  laptop: '<rect x="4" y="4" width="16" height="12" rx="2"/><path d="M2 20h20l-2-4H4l-2 4Z"/><path d="M10 18h4"/>',
  close: '<path d="m6 6 12 12M6 18 18 6"/>',
  refresh: '<path d="M20 7v5h-5M4 17v-5h5"/><path d="M6.1 7a7 7 0 0 1 11.5-1L20 9M4 15l2.4 3A7 7 0 0 0 18 17"/>',
  menu: '<path d="M4 6h16M4 12h16M4 18h16"/>',
  star: '<path d="m12 3 2.8 5.7 6.2.9-4.5 4.4 1.1 6.2-5.6-3-5.6 3 1.1-6.2L3 9.6l6.2-.9L12 3Z"/>',
  trash: '<path d="M4 7h16M10 11v6M14 11v6M5 7l1 13h12l1-13M9 7V4h6v3"/>',
  home: '<path d="m3 11 9-7 9 7v9a1 1 0 0 1-1 1h-5v-6h-6v6H4a1 1 0 0 1-1-1v-9Z"/>',
  send: '<path d="M21 3 10 14M21 3l-7 18-4-7-7-4 18-7Z"/>',
} as const;

export type IconName = keyof typeof ICONS;

interface IconProps {
  name: IconName;
  className?: string;
  title?: string;
}

export const Icon: React.FC<IconProps> = ({ name, className = '', title }) => (
  <svg
    className={`icon ${className}`}
    width="24"
    height="24"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth={1.7}
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden={title ? undefined : true}
    role={title ? 'img' : undefined}
    aria-label={title}
    // Nội dung SVG là hằng số tĩnh trong file, không chứa dữ liệu người dùng.
    dangerouslySetInnerHTML={{ __html: ICONS[name] }}
  />
);
