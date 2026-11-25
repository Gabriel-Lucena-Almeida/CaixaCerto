import { clsx } from 'clsx';

interface ChipProps {
  label: string;
  value: string;
  selected: boolean;
  onClick: () => void;
  disabled?: boolean;
}

export function Chip({ label, value, selected, onClick, disabled = false }: ChipProps) {
  return (
    <label className="chip">
      <input
        type="radio"
        name="category"
        value={value}
        checked={selected}
        onChange={onClick}
        disabled={disabled}
        className="absolute inset-0 opacity-0"
      />
      <span className={clsx(
        'inline-flex items-center h-11 px-4 border border-gray-300 rounded-full bg-white text-gray-500 text-sm transition-all cursor-pointer',
        selected && 'border-2 border-dark-900 text-dark-900 font-bold',
        disabled && 'opacity-50 cursor-not-allowed'
      )}>
        {label}
      </span>
    </label>
  );
}

interface ChipsProps {
  options: string[];
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
  className?: string;
}

export function Chips({ options, value, onChange, disabled = false, className }: ChipsProps) {
  return (
    <div className={clsx('chips', className)} role="radiogroup" aria-label="Categoria">
      {options.map((option) => (
        <Chip
          key={option}
          label={option}
          value={option}
          selected={value === option}
          onClick={() => onChange(option)}
          disabled={disabled}
        />
      ))}
    </div>
  );
}