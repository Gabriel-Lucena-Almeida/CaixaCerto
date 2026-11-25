import { ButtonHTMLAttributes, forwardRef } from 'react';
import { clsx } from 'clsx';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'success' | 'danger' | 'outline' | 'ghost' | 'link';
  size?: 'sm' | 'md' | 'lg';
  fullWidth?: boolean;
  loading?: boolean;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'primary', size = 'md', fullWidth = false, loading = false, disabled, children, ...props }, ref) => {
    const baseClasses = 'inline-flex items-center justify-center font-heading font-bold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed';

    const variantClasses = {
      primary: 'bg-dark-900 text-white hover:bg-dark-800 focus-visible:ring-dark-900',
      success: 'bg-primary-500 text-white hover:bg-primary-600 focus-visible:ring-primary-500',
      danger: 'bg-accent-red text-white hover:bg-red-700 focus-visible:ring-accent-red',
      outline: 'bg-white border border-gray-300 text-dark-900 hover:bg-gray-50 focus-visible:ring-dark-900',
      ghost: 'bg-transparent text-primary-500 hover:bg-primary-50 focus-visible:ring-primary-500',
      link: 'text-primary-500 font-bold underline underline-offset-2 hover:text-primary-600',
    };

    const sizeClasses = {
      sm: 'h-10 px-4 text-sm rounded-xl',
      md: 'h-14 px-6 text-base rounded-2xl',
      lg: 'h-16 px-8 text-lg rounded-2xl',
    };

    const widthClass = fullWidth ? 'w-full' : '';

    return (
      <button
        ref={ref}
        className={clsx(baseClasses, variantClasses[variant], sizeClasses[size], widthClass, className)}
        disabled={disabled || loading}
        {...props}
      >
        {loading && (
          <svg className="animate-spin -ml-1 mr-2 h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
          </svg>
        )}
        {children}
      </button>
    );
  }
);

Button.displayName = 'Button';