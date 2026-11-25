package br.ufpb.caixacerto.config;

import br.ufpb.caixacerto.common.enums.MovementNature;
import br.ufpb.caixacerto.common.enums.MovementType;
import br.ufpb.caixacerto.common.utils.MoneyUtils;
import br.ufpb.caixacerto.movement.Movement;
import br.ufpb.caixacerto.movement.MovementRepository;
import br.ufpb.caixacerto.user.User;
import br.ufpb.caixacerto.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Base64;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataLoader implements ApplicationRunner {

    private static final String DEMO_EMAIL = "demo@caixacerto.local";
    private static final String DEMO_PASSWORD = "demo1234";
    private static final String DEMO_NAME = "Demo";

    private final UserRepository userRepository;
    private final MovementRepository movementRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!args.containsOption("demo")) {
            return;
        }

        log.info("Carregando dados de demonstração...");

        Optional<User> existing = userRepository.findByEmailIgnoreCase(DEMO_EMAIL);
        if (existing.isPresent()) {
            log.info("Conta de demonstração já existe");
            return;
        }

        // Criar usuário demo
        String salt = generateSalt();
        String passwordHash = passwordEncoder.encode(DEMO_PASSWORD + salt);

        User demoUser = User.builder()
                .name(DEMO_NAME)
                .email(DEMO_EMAIL)
                .passwordHash(passwordHash)
                .passwordSalt(salt)
                .consentGiven(true)
                .build();

        demoUser = userRepository.save(demoUser);

        // Criar movimentações de exemplo
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);

        // Entradas do negócio
        addMovement(demoUser, inicioMes.plusDays(2), MovementNature.ENTRADA, MovementType.NEGOCIO,
                "Receita", (int) MoneyUtils.toCents("1420,00"), "Atendimentos da primeira semana");
        addMovement(demoUser, inicioMes.plusDays(9), MovementNature.ENTRADA, MovementType.NEGOCIO,
                "Receita", (int) MoneyUtils.toCents("1500,00"), "Atendimentos da segunda semana");
        addMovement(demoUser, inicioMes.plusDays(16), MovementNature.ENTRADA, MovementType.NEGOCIO,
                "Receita", (int) MoneyUtils.toCents("1200,00"), "Atendimentos da terceira semana");
        addMovement(demoUser, hoje, MovementNature.ENTRADA, MovementType.NEGOCIO,
                "Receita", (int) MoneyUtils.toCents("180,00"), "Atendimento de cliente");

        // Saídas do negócio
        addMovement(demoUser, inicioMes.plusDays(1), MovementNature.SAIDA, MovementType.NEGOCIO,
                "Aluguel", (int) MoneyUtils.toCents("1200,00"), "Aluguel do espaço");
        addMovement(demoUser, inicioMes.plusDays(5), MovementNature.SAIDA, MovementType.NEGOCIO,
                "Divulgação", (int) MoneyUtils.toCents("450,00"), "Anúncios nas redes sociais");
        addMovement(demoUser, inicioMes.plusDays(7), MovementNature.SAIDA, MovementType.NEGOCIO,
                "Taxas", (int) MoneyUtils.toCents("415,00"), "Taxas da maquininha");
        addMovement(demoUser, hoje.minusDays(1).isBefore(inicioMes) ? inicioMes : hoje.minusDays(1),
                MovementNature.SAIDA, MovementType.NEGOCIO,
                "Material", (int) MoneyUtils.toCents("95,00"), "Compra de material");

        // Saídas pessoais
        addMovement(demoUser, inicioMes.plusDays(3), MovementNature.SAIDA, MovementType.PESSOAL,
                "Outros", (int) MoneyUtils.toCents("740,00"), "Contas de casa");
        addMovement(demoUser, hoje.minusDays(1).isBefore(inicioMes) ? inicioMes : hoje.minusDays(1),
                MovementNature.SAIDA, MovementType.PESSOAL,
                "Outros", (int) MoneyUtils.toCents("120,00"), "Mercado");

        log.info("Dados de demonstração carregados com sucesso!");
        log.info("Login: {} / {}", DEMO_EMAIL, DEMO_PASSWORD);
    }

    private void addMovement(User user, LocalDate date, MovementNature nature, MovementType type,
                             String category, int amountCents, String description) {
        Movement movement = Movement.builder()
                .user(user)
                .date(date)
                .nature(nature)
                .type(type)
                .category(category)
                .amountCents(amountCents)
                .description(description)
                .build();
        movementRepository.save(movement);
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}