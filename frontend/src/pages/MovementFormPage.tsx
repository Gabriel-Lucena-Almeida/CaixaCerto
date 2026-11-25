import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useCreateMovement, useUpdateMovement, useMovement } from '../api/hooks';
import { format, toCents } from '../utils/money';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Alert } from '../ui/Alert';
import { RadioGroup, RadioOption } from '../ui/RadioGroup';
import { Chips } from '../ui/Chip';
import { Card } from '../ui/Card';
import { DEFAULT_CATEGORIES, DEFAULT_CATEGORY, MovementNature, MovementType } from '../api/types';
import { ArrowLeft, ChevronRight } from 'lucide-react';

const movementSchema = z.object({
  nature: z.enum(['ENTRADA', 'SAIDA']),
  type: z.enum(['NEGOCIO', 'PESSOAL']),
  category: z.string().min(1, 'Categoria é obrigatória'),
  value: z.string().min(1, 'Informe o valor'),
  description: z.string().min(1, 'Escreva uma descrição').max(80, 'A descrição pode ter até 80 caracteres'),
  date: z.string().min(1, 'Informe uma data válida'),
});

type MovementForm = z.infer<typeof movementSchema>;

const NATURE_OPTIONS = [
  { value: 'SAIDA', label: 'Saída' },
  { value: 'ENTRADA', label: 'Entrada' },
] as const;

const TYPE_OPTIONS = [
  { value: 'NEGOCIO', label: 'Negócio' },
  { value: 'PESSOAL', label: 'Pessoal' },
] as const;

export function MovementFormPage() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

  const createMutation = useCreateMovement();
  const updateMutation = useUpdateMovement();
  const { data: existingMovement } = useMovement(id ? parseInt(id) : 0, { enabled: isEditing });

  const [error, setError] = useState<string | null>(null);
  const [showCategories, setShowCategories] = useState(true);

  const formatForInput = (cents: number) => (cents / 100).toFixed(2).replace('.', ',');

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<MovementForm>({
    resolver: zodResolver(movementSchema),
    defaultValues: {
      nature: 'SAIDA',
      type: 'NEGOCIO',
      category: DEFAULT_CATEGORY,
      value: '',
      description: '',
      date: new Date().toISOString().split('T')[0],
    },
  });

  const nature = watch('nature');
  const type = watch('type');

  // Atualiza categoria quando natureza muda
  useEffect(() => {
    if (nature === 'ENTRADA') {
      setValue('category', 'Receita', { shouldValidate: true });
      setShowCategories(false);
    } else {
      setShowCategories(true);
      // Se categoria atual é "Receita", volta para padrão
      const currentCategory = watch('category');
      if (currentCategory === 'Receita') {
        setValue('category', DEFAULT_CATEGORY, { shouldValidate: true });
      }
    }
  }, [nature, setValue, watch]);

  // Preenche formulário se editando
  useEffect(() => {
    if (isEditing && existingMovement) {
      setValue('nature', existingMovement.nature);
      setValue('type', existingMovement.type);
      setValue('category', existingMovement.category);
      setValue('value', formatForInput(existingMovement.amountCents));
      setValue('description', existingMovement.description);
      setValue('date', existingMovement.date);
      setShowCategories(existingMovement.nature !== 'ENTRADA');
    }
  }, [isEditing, existingMovement, setValue]);

  const onSubmit = async (data: MovementForm) => {
    setError(null);
    try {
      const amountCents = toCents(data.value);
      const payload = {
        nature: data.nature as MovementNature,
        type: data.type as MovementType,
        category: data.category,
        value: data.value,
        description: data.description,
        date: data.date,
      };

      if (isEditing) {
        await updateMutation.mutateAsync({ id: parseInt(id!), data: payload });
      } else {
        await createMutation.mutateAsync(payload);
      }

      navigate(-1); // Volta para a página anterior
    } catch (err: any) {
      setError(err?.message || 'Erro ao salvar. Tente novamente.');
    }
  };

  return (
    <div className="flex-1 flex flex-col">
      <header className="header-with-back p-5 border-b border-gray-100 bg-white sticky top-0 z-10">
        <button onClick={() => navigate(-1)} className="back-button" aria-label="Voltar">
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="text-xl font-heading font-bold">{isEditing ? 'Editar movimentação' : 'Nova movimentação'}</h1>
      </header>

      <main className="page-container">
        {error && <Alert message={error} onDismiss={() => setError(null)} />}

        <form onSubmit={handleSubmit(onSubmit)} className="form space-y-5" noValidate>
          <div className="form-section space-y-5">
            <fieldset>
              <legend className="label">Entrada ou saída</legend>
              <RadioGroup
                name="natureza"
                value={nature}
                onChange={(v) => setValue('nature', v)}
                options={NATURE_OPTIONS}
                variant="segmented"
                inline
              />
            </fieldset>

            <Input
              {...register('value')}
              label="Valor"
              placeholder="R$ 0,00"
              inputMode="decimal"
              autoComplete="off"
              error={errors.value?.message}
              className="input-lg"
              onChange={(e) => {
                // Formata enquanto digita
                let value = e.target.value.replace(/\D/g, '');
                if (value.length <= 2) {
                  value = (parseInt(value) / 100).toFixed(2).replace('.', ',');
                } else {
                  value = value.replace(/(\d{2})$/, ',$1').replace(/\B(?=(\d{3})+(?!\d))/g, '.');
                }
                setValue('value', value, { shouldValidate: true });
              }}
            />

            <Input
              {...register('description')}
              label="Descrição"
              placeholder="Ex.: Compra de material"
              autoComplete="off"
              maxLength={80}
              error={errors.description?.message}
            />

            <fieldset>
              <legend className="label">Essa movimentação é do…</legend>
              <RadioGroup
                name="tipo"
                value={type}
                onChange={(v) => setValue('type', v)}
                options={TYPE_OPTIONS}
                variant="box"
              />
            </fieldset>

            <fieldset className={showCategories ? '' : 'hidden'}>
              <legend className="label">Categoria</legend>
              <Chips
                options={nature === 'ENTRADA' ? ['Receita'] : DEFAULT_CATEGORIES}
                value={watch('category')}
                onChange={(v) => setValue('category', v)}
              />
            </fieldset>

            <Input
              {...register('date')}
              type="date"
              label="Data"
              max={new Date().toISOString().split('T')[0]}
              error={errors.date?.message}
            />
          </div>

          <Button type="submit" className="w-full empurra" size="lg" loading={createMutation.isPending || updateMutation.isPending || isSubmitting}>
            Salvar
          </Button>
        </form>
      </main>
    </div>
  );
}