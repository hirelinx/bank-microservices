package ru.practicum.bankui.web.contoller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.bankui.exception.BankServiceException;
import ru.practicum.bankui.service.AccountServiceImpl;
import ru.practicum.bankui.web.dto.AccountDto;
import ru.practicum.bankui.web.dto.CashAction;
import ru.practicum.bankui.web.dto.UserDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Контроллер main.html.
 *
 * Поля модели:
 *      login - логин текущего пользователя, String
 *      name - Фамилия Имя текущего пользователя, String
 *      birthdate - дата рождения текущего пользователя, String в формате 'YYYY-MM-DD'
 *      sum - сумма на счету текущего пользователя, BigDecimal
 *      accounts - список аккаунтов, которым можно перевести деньги, List<AccountDto>
 *      errors - список ошибок после выполнения действий, List<String> (не обязательное)
 *      info - строка успешности после выполнения действия, String (не обязательное)
 *
 * Все запросы в микросервисы идут через Gateway API с JWT текущего пользователя.
 */
@Controller
@RequiredArgsConstructor
public class MainController {
    private final AccountServiceImpl accountService;

    @GetMapping
    public String index() {
        return "redirect:/account";
    }

    @GetMapping("/account")
    public String getAccount(Model model) {
        return render(model, null, null);
    }

    @PostMapping("/account")
    public String editAccount(
            Model model,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "birthdate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthdate
    ) {
        return execute(model, () -> {
            accountService.updateCurrentAccount(name, birthdate);
            return "Данные аккаунта сохранены";
        });
    }

    @PostMapping("/cash")
    public String editCash(
            Model model,
            @RequestParam(value = "value", required = false) String value,
            @RequestParam("action") CashAction action
    ) {
        return execute(model, () -> accountService.editCash(action, value));
    }

    @PostMapping("/transfer")
    public String transfer(
            Model model,
            @RequestParam(value = "value", required = false) String value,
            @RequestParam(value = "login", required = false) String login
    ) {
        return execute(model, () -> accountService.transfer(login, value));
    }

    private String execute(Model model, Supplier<String> action) {
        try {
            return render(model, null, action.get());
        } catch (BankServiceException e) {
            return render(model, e.getErrors(), null);
        }
    }

    private String render(Model model, List<String> actionErrors, String info) {
        List<String> errors = new ArrayList<>();
        if (actionErrors != null) {
            errors.addAll(actionErrors);
        }

        try {
            UserDto user = accountService.getCurrentAccount();
            model.addAttribute("login", user.login());
            model.addAttribute("name", user.name());
            model.addAttribute("birthdate", user.birthdate() == null ? null : user.birthdate().format(DateTimeFormatter.ISO_DATE));
            model.addAttribute("sum", user.balance());
        } catch (BankServiceException e) {
            errors.addAll(e.getErrors());
        }

        List<AccountDto> accounts = List.of();
        try {
            accounts = accountService.getOtherAccounts();
        } catch (BankServiceException e) {
            errors.addAll(e.getErrors());
        }
        model.addAttribute("accounts", accounts);

        model.addAttribute("errors", errors.isEmpty() ? null : errors.stream().distinct().toList());
        model.addAttribute("info", info);
        return "main";
    }
}
