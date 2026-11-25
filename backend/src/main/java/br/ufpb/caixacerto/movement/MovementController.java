package br.ufpb.caixacerto.movement;

import br.ufpb.caixacerto.common.enums.MovementNature;
import br.ufpb.caixacerto.common.enums.MovementType;
import br.ufpb.caixacerto.common.exception.BusinessRuleException;
import br.ufpb.caixacerto.user.User;
import br.ufpb.caixacerto.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/movements")
@RequiredArgsConstructor
@Slf4j
public class MovementController {

    private final MovementService movementService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<MovementDTOs.Response> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MovementDTOs.CreateRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String field = bindingResult.getFieldErrors().get(0).getField();
            String message = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            throw new BusinessRuleException(message, field);
        }

        User user = userService.getUserFromDetails(userDetails);
        MovementDTOs.Response response = movementService.create(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<MovementDTOs.PageResponse> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String nature,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User user = userService.getUserFromDetails(userDetails);

        MovementType mType = type != null ? MovementType.valueOf(type.toUpperCase()) : null;
        MovementNature mNature = nature != null ? MovementNature.valueOf(nature.toUpperCase()) : null;

        MovementDTOs.PageResponse response;
        if (startDate != null && endDate != null) {
            response = movementService.list(user, MovementDTOs.FilterRequest.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .page(page)
                    .size(size)
                    .build());
        } else if (mType != null && mNature != null) {
            List<MovementDTOs.Response> movements = movementService.list(user, MovementDTOs.FilterRequest.builder()
                    .type(mType)
                    .nature(mNature)
                    .page(page)
                    .size(size)
                    .build()).getContent();
            response = MovementDTOs.PageResponse.builder()
                    .content(movements)
                    .page(page)
                    .size(size)
                    .totalElements((long) movements.size())
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();
        } else if (mType != null) {
            List<MovementDTOs.Response> movements = movementService.list(user, MovementDTOs.FilterRequest.builder()
                    .type(mType)
                    .page(page)
                    .size(size)
                    .build()).getContent();
            response = MovementDTOs.PageResponse.builder()
                    .content(movements)
                    .page(page)
                    .size(size)
                    .totalElements((long) movements.size())
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();
        } else {
            response = movementService.list(user, MovementDTOs.FilterRequest.builder()
                    .page(page)
                    .size(size)
                    .build());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<MovementDTOs.Response>> getRecent(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String type) {

        User user = userService.getUserFromDetails(userDetails);
        MovementType mType = MovementType.valueOf(type.toUpperCase());
        List<MovementDTOs.Response> movements = movementService.listLastThree(user, mType);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovementDTOs.Response> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        User user = userService.getUserFromDetails(userDetails);
        MovementDTOs.Response response = movementService.getById(user, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovementDTOs.Response> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody MovementDTOs.UpdateRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String field = bindingResult.getFieldErrors().get(0).getField();
            String message = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            throw new BusinessRuleException(message, field);
        }

        User user = userService.getUserFromDetails(userDetails);
        MovementDTOs.Response response = movementService.update(user, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        User user = userService.getUserFromDetails(userDetails);
        movementService.delete(user, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryDTOs.Response> getMonthSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String month) {

        User user = userService.getUserFromDetails(userDetails);
        YearMonth ym = month != null ? YearMonth.parse(month, DateTimeFormatter.ISO_DATE) : YearMonth.now();
        SummaryDTOs.Response summary = movementService.getMonthSummary(user, ym);
        return ResponseEntity.ok(summary);
    }
}