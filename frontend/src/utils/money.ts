/**
 * Utilitários para manipulação de valores monetários.
 * Port do MoneyUtils.java / Dinheiro.java original.
 * Internamente tudo é guardado em centavos (number) para evitar erros de ponto flutuante.
 */

const PT_BR_LOCALE = 'pt-BR';
const MAX_VALUE = 9999999.99;

const currencyFormatter = new Intl.NumberFormat(PT_BR_LOCALE, {
  style: 'currency',
  currency: 'BRL',
});

const integerFormatter = new Intl.NumberFormat(PT_BR_LOCALE, {
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
});

/**
 * Converte string para centavos.
 * Aceita: "95", "95,5", "1.250,50", "R$ 95,00", "95.00"
 *
 * @throws Error se valor inválido
 */
export function toCents(text: string): number {
  if (!text || text.trim() === '') {
    throw new Error('Informe o valor.');
  }

  let t = text
    .replace('R$', '')
    .replace(' ', '') // non-breaking space
    .replace(' ', '')
    .trim();

  // Formato brasileiro: 1.250,50 -> 1250.50
  if (t.includes(',')) {
    t = t.replace(/\./g, '').replace(',', '.');
  } else if (/^\d{1,3}(\.\d{3})+$/.test(t)) {
    // 1.250 = mil duzentos e cinquenta (sem centavos)
    t = t.replace(/\./g, '');
  }

  let value: number;
  try {
    value = parseFloat(t);
    if (isNaN(value)) throw new Error();
  } catch {
    throw new Error('Valor inválido. Use números, como 95,00.');
  }

  // Arredonda para 2 casas decimais
  value = Math.round(value * 100) / 100;

  if (value <= 0) {
    throw new Error('O valor precisa ser maior que zero.');
  }

  if (value > MAX_VALUE) {
    throw new Error('Valor muito alto (máximo R$ 9.999.999,99).');
  }

  return Math.round(value * 100);
}

/**
 * Formata centavos para "R$ 1.250,00".
 * Valores negativos: "− R$ 10,00".
 */
export function format(cents: number): string {
  const sign = cents < 0 ? '− ' : '';
  return sign + currencyFormatter.format(Math.abs(cents) / 100);
}

/**
 * Formata centavos para "1.250,00" (sem R$).
 */
export function formatNumber(cents: number): string {
  return currencyFormatter.format(Math.abs(cents) / 100).replace('R$', '').trim();
}

/**
 * Formata centavos para "R$ 1.250" (sem centavos, para cards compactos).
 */
export function formatInteger(cents: number): string {
  const sign = cents < 0 ? '− ' : '';
  const reais = Math.floor(Math.abs(cents) / 100);
  return sign + 'R$ ' + integerFormatter.format(reais);
}

/**
 * Formata apenas o número inteiro (sem R$, sem centavos).
 */
export function formatIntegerNumber(cents: number): string {
  const reais = Math.floor(Math.abs(cents) / 100);
  return integerFormatter.format(reais);
}

/**
 * Converte centavos para número com 2 casas decimais.
 */
export function toNumber(cents: number): number {
  return cents / 100;
}

/**
 * Soma segura de centavos.
 */
export function add(a: number, b: number): number {
  return a + b;
}

/**
 * Subtração segura de centavos.
 */
export function subtract(a: number, b: number): number {
  return a - b;
}

/**
 * Formata para input (valor bruto com vírgula).
 */
export function formatForInput(cents: number): string {
  return (cents / 100).toFixed(2).replace('.', ',');
}

/**
 * Parse do valor do input para centavos.
 */
export function parseInputValue(value: string): number {
  return toCents(value);
}