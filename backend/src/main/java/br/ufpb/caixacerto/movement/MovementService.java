package br.ufpb.caixacerto.movement;

import br.ufpb.caixacerto.common.enums.MovementNature;
import br.ufpb.caixacerto.common.enums.MovementType;
import br.ufpb.caixacerto.common.exception.BusinessRuleException;
import br.ufpb.caixacerto.common.exception.ResourceNotFoundException;
import br.ufpb.caixacerto.common.utils.MoneyUtils;
import br.ufpb.caixacerto.user.User;
import br.ufpb.caixacerto.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementService {

    private static final List<String> DEFAULT_CATEGORIES = List.of("Material", "Aluguel", "Divulgação", "Taxas", "Outros");
    private static final String DEFAULT_CATEGORY = "Outros";

    private final MovementRepository movementRepository;
    private final UserRepository userRepository;

    @Transactional
    public MovementDTOs.Response create(User user, MovementDTOs.CreateRequest request) {
        validateCreateRequest(request);

        LocalDate date = request.getDate();
        if (date.isAfter(LocalDate.now())) {
            throw new BusinessRuleException("A data não pode ser no futuro.", "date");
        }

        MovementNature nature = request.getNature();
        MovementType type = request.getType();
        String category = request.getCategory();

        // Se for entrada, categoria fixa como "Receita"
        if (nature == MovementNature.ENTRADA) {
            category = "Receita";
        } else if (!DEFAULT_CATEGORIES.contains(category)) {
            category = DEFAULT_CATEGORY;
        }

        int amountCents = (int) MoneyUtils.toCents(request.getValue());

        Movement movement = Movement.builder()
                .user(user)
                .date(date)
                .nature(nature)
                .type(type)
                .category(category)
                .amountCents(amountCents)
                .description(request.getDescription().trim())
                .build();

        movement = movementRepository.save(movement);
        log.info("Movimentação criada: userId={}, id={}", user.getId(), movement.getId());

        return toResponse(movement);
    }

    @Transactional(readOnly = true)
    public MovementDTOs.PageResponse list(User user, MovementDTOs.FilterRequest filter) {
        int page = Math.max(0, filter.getPage());
        int size = Math.min(100, Math.max(1, filter.getSize()));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "id"));

        Page<Movement> movementPage;

        if (filter.getStartDate() != null && filter.getEndDate() != null) {
            movementPage = movementRepository.findByUserAndDateBetween(user, filter.getStartDate(), filter.getEndDate(), pageable);
        } else if (filter.getType() != null && filter.getNature() != null) {
            List<Movement> movements = movementRepository.findByUserAndNatureAndTypeOrderByDateDescIdDesc(user, filter.getNature(), filter.getType());
            // Para simplificar, não pagino aqui - em produção usaria query customizada
            movementPage = new org.springframework.data.domain.PageImpl<>(movements, pageable, movements.size());
        } else if (filter.getType() != null) {
            List<Movement> movements = movementRepository.findByUserAndTypeOrderByDateDescIdDesc(user, filter.getType());
            movementPage = new org.springframework.data.domain.PageImpl<>(movements, pageable, movements.size());
        } else {
            movementPage = movementRepository.findByUserOrderByDateDescIdDesc(user, pageable);
        }

        return MovementDTOs.PageResponse.builder()
                .content(movementPage.getContent().stream().map(this::toResponse).toList())
                .page(movementPage.getNumber())
                .size(movementPage.getSize())
                .totalElements(movementPage.getTotalElements())
                .totalPages(movementPage.getTotalPages())
                .first(movementPage.isFirst())
                .last(movementPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<MovementDTOs.Response> listLastThree(User user, MovementType type) {
        List<Movement> movements = movementRepository.findByUserAndTypeOrderByDateDescIdDesc(user, type);
        return movements.stream()
                .limit(3)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovementDTOs.Response getById(User user, Long id) {
        Movement movement = movementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada."));

        if (!movement.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Movimentação não encontrada.");
        }

        return toResponse(movement);
    }

    @Transactional
    public MovementDTOs.Response update(User user, Long id, MovementDTOs.UpdateRequest request) {
        Movement movement = movementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada."));

        if (!movement.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Movimentação não encontrada.");
        }

        if (request.getNature() != null) {
            movement.setNature(request.getNature());
        }
        if (request.getType() != null) {
            movement.setType(request.getType());
        }
        if (request.getCategory() != null) {
            String category = request.getCategory();
            if (movement.getNature() == MovementNature.ENTRADA) {
                category = "Receita";
            } else if (!DEFAULT_CATEGORIES.contains(category)) {
                category = DEFAULT_CATEGORY;
            }
            movement.setCategory(category);
        }
        if (request.getValue() != null) {
            movement.setAmountCents((int) MoneyUtils.toCents(request.getValue()));
        }
        if (request.getDescription() != null) {
            movement.setDescription(request.getDescription().trim());
        }
        if (request.getDate() != null) {
            if (request.getDate().isAfter(LocalDate.now())) {
                throw new BusinessRuleException("A data não pode ser no futuro.", "date");
            }
            movement.setDate(request.getDate());
        }

        movement = movementRepository.save(movement);
        log.info("Movimentação atualizada: userId={}, id={}", user.getId(), id);

        return toResponse(movement);
    }

    @Transactional
    public void delete(User user, Long id) {
        Movement movement = movementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada."));

        if (!movement.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Movimentação não encontrada.");
        }

        movementRepository.delete(movement);
        log.info("Movimentação excluída: userId={}, id={}", user.getId(), id);
    }

    @Transactional(readOnly = true)
    public SummaryDTOs.Response getMonthSummary(User user, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        Long entradasNegocio = movementRepository.sumAmountByUserNatureTypeAndDateBetween(
                user, MovementNature.ENTRADA, MovementType.NEGOCIO, start, end);
        Long saidasNegocio = movementRepository.sumAmountByUserNatureTypeAndDateBetween(
                user, MovementNature.SAIDA, MovementType.NEGOCIO, start, end);
        Long entradasPessoal = movementRepository.sumAmountByUserNatureTypeAndDateBetween(
                user, MovementNature.ENTRADA, MovementType.PESSOAL, start, end);
        Long saidasPessoal = movementRepository.sumAmountByUserNatureTypeAndDateBetween(
                user, MovementNature.SAIDA, MovementType.PESSOAL, start, end);

        entradasNegocio = entradasNegocio != null ? entradasNegocio : 0L;
        saidasNegocio = saidasNegocio != null ? saidasNegocio : 0L;
        entradasPessoal = entradasPessoal != null ? entradasPessoal : 0L;
        saidasPessoal = saidasPessoal != null ? saidasPessoal : 0L;

        long lucroReal = entradasNegocio - saidasNegocio;
        long saldoPessoal = entradasPessoal - saidasPessoal;
        long sobraDoMes = lucroReal + saldoPessoal;

        return SummaryDTOs.Response.builder()
                .month(month)
                .entradasNegocio(entradasNegocio)
                .saidasNegocio(saidasNegocio)
                .entradasPessoal(entradasPessoal)
                .saidasPessoal(saidasPessoal)
                .lucroReal(lucroReal)
                .saldoPessoal(saldoPessoal)
                .sobraDoMes(sobraDoMes)
                .formattedEntradasNegocio(MoneyUtils.format(entradasNegocio))
                .formattedSaidasNegocio(MoneyUtils.format(saidasNegocio))
                .formattedEntradasPessoal(MoneyUtils.format(entradasPessoal))
                .formattedSaidasPessoal(MoneyUtils.format(saidasPessoal))
                .formattedLucroReal(MoneyUtils.format(lucroReal))
                .formattedSaldoPessoal(MoneyUtils.format(saldoPessoal))
                .formattedSobraDoMes(MoneyUtils.format(sobraDoMes))
                .build();
    }

    private void validateCreateRequest(MovementDTOs.CreateRequest request) {
        if (request.getNature() == null) {
            throw new BusinessRuleException("Escolha entrada ou saída.", "nature");
        }
        if (request.getType() == null) {
            throw new BusinessRuleException("Escolha se é do negócio ou pessoal.", "type");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new BusinessRuleException("Escreva uma descrição.", "description");
        }
        if (request.getDescription().trim().length() > 80) {
            throw new BusinessRuleException("A descrição pode ter até 80 caracteres.", "description");
        }
        if (request.getDate() == null) {
            throw new BusinessRuleException("Informe uma data válida.", "date");
        }
        // Valor será validado no MoneyUtils.toCents
    }

    private MovementDTOs.Response toResponse(Movement movement) {
        return MovementDTOs.Response.builder()
                .id(movement.getId())
                .date(movement.getDate())
                .nature(movement.getNature())
                .type(movement.getType())
                .category(movement.getCategory())
                .amountCents(movement.getAmountCents())
                .formattedValue(MoneyUtils.format(movement.getAmountCents().longValue()))
                .description(movement.getDescription())
                .createdAt(movement.getCreatedAt().toString())
                .build();
    }
}