import { RadioHTMLAttributes, forwardRef } from 'react';
import { clsx } from 'clsx';

interface RadioOption {
  value: string;
  label: string;
}

interface RadioGroupProps {
  name: string;
  value: string;
  onChange: (value: string) => void;
  options: RadioOption[];
  className?: string;
  inline?: boolean;
  variant?: 'default' | 'box' | 'segmented';
  disabled?: boolean;
}

export function RadioGroup({ name, value, onChange, options, className, inline = false, variant = 'default', disabled = false }: RadioGroupProps) {
  if (variant === 'segmented') {
    return (
      <div className={clsx('radio-group', inline && 'flex-1', className)} role="radiogroup" aria-label={name}>
        {options.map((option) => (
          <label key={option.value} className="radio-option">
            <input
              type="radio"
              name={name}
              value={option.value}
              checked={value === option.value}
              onChange={() => onChange(option.value)}
              disabled={disabled}
              className="absolute inset-0 opacity-0"
            />
            <span className={clsx(
              'block py-3 rounded-lg text-sm font-medium transition-all',
              value === option.value ? 'bg-white font-bold shadow-sm' : 'text-gray-500'
            )}>
              {option.label}
            </span>
          </label>
        ))}
      </div>
    );
  }

  if (variant === 'box') {
    return (
      <div className={clsx('radio-group-box', className)} role="radiogroup" aria-label={name}>
        {options.map((option) => (
          <label key={option.value} className="radio-box">
            <input
              type="radio"
              name={name}
              value={option.value}
              checked={value === option.value}
              onChange={() => onChange(option.value)}
              disabled={disabled}
              className="absolute inset-0 opacity-0"
            />
            <span className={clsx(
              'flex items-center justify-center h-13 border border-gray-300 rounded-xl bg-white text-gray-500 text-base transition-all',
              value === option.value && 'border-2 border-primary-500 bg-primary-50 text-primary-700 font-bold'
            )}>
              {option.label}
            </span>
          </label>
        ))}
      </div>
    );
  }

  // Default inline radio buttons
  return (
    <fieldset className={clsx('space-y-2', className)}>
      <legend className="label">{name}</legend>
      {options.map((option) => (
        <label key={option.value} className="flex items-center gap-3 cursor-pointer">
          <input
            type="radio"
            name={name}
            value={option.value}
            checked={value === option.value}
            onChange={() => onChange(option.value)}
            disabled={disabled}
            className="w-5 h-5 text-primary-500 border-gray-300 focus:ring-primary-500"
          />
          <span className="text-base text-dark-900">{option.label}</span>
        </label>
      ))}
    </fieldset>
  );
}

export function RadioOption({ value, label, checked, onChange, disabled, className, variant = 'default' }: RadioHTMLAttributes<HTMLInputElement> & {
  label: string;
  checked: boolean;
  onChange: () => void;
  variant?: 'default' | 'expense' | 'income';
}) {
  if (variant === 'expense' || variant === 'income') {
    return (
      <label className="radio-option">
        <input
          type="radio"
          value={value}
          checked={checked}
          onChange={onChange}
          disabled={disabled}
          className="absolute inset-0 opacity-0"
        />
        <span className={clsx(
          'block py-3 rounded-lg text-sm font-medium transition-all',
          checked ? 'bg-white font-bold shadow-sm' : 'text-gray-500',
          variant === 'expense' && checked && 'text-accent-red',
          variant === 'income' && checked && 'text-primary-500'
        )}>
          {label}
        </span>
      </label>
    );
  }

  return (
    <label className={clsx('flex items-center gap-3 cursor-pointer', className)}>
      <input
        type="radio"
        value={value}
        checked={checked}
        onChange={onChange}
        disabled={disabled}
        className="w-5 h-5 text-primary-500 border-gray-300 focus:ring-primary-500"
      />
      <span className="text-base text-dark-900">{label}</span>
    </label>
  );
}