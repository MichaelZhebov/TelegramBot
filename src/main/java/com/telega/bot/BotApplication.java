package com.telega.bot;

import com.telega.bot.model.Employee;
import com.telega.bot.service.NetworkService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotApplication extends TelegramLongPollingBot {

    private List<Employee> employees;
    private Map<String, Boolean> actionsMap = new HashMap<>();
    private NetworkService networkService = NetworkService.getInstance();

    {
        actionsMap.put("DELETE", false);
        actionsMap.put("UPDATE", false);
        actionsMap.put("ADD NEW", false);
    }

    @Override
    public void onUpdateReceived(Update update) {
        StringBuilder message = new StringBuilder();
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                for (Map.Entry<String, Boolean> entry : actionsMap.entrySet()
                ) {
                    if (entry.getValue()) {
                        if (entry.getKey().equals("DELETE")) {
                            actionsMap.put("DELETE", false);
                            long id = Long.parseLong(update.getMessage().getText());
                            message.append(deleteEmployees(id) ? "Deleted" : "Error. Please try again");
                            try {
                                execute(new SendMessage().setChatId(update.getMessage().getChatId())
                                        .setText(message.toString()));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                        if (entry.getKey().equals("ADD NEW")) {
                            actionsMap.put("ADD NEW", false);
                            Employee employee = new Employee();
                            String[] employeeFields = update.getMessage().getText().split(" ");
                            employee.setFirstName(employeeFields[0]);
                            employee.setLastName(employeeFields[1]);
                            employee.setEmail(employeeFields[2]);
                            message.append(addEmployees(employee) ? "Added" : "Error. Please try again");
                            try {
                                execute(new SendMessage().setChatId(update.getMessage().getChatId())
                                        .setText(message.toString()));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                        if (entry.getKey().equals("UPDATE")) {
                            actionsMap.put("UPDATE", false);
                            Employee employee = new Employee();
                            String[] employeeFields = update.getMessage().getText().split(" ");
                            employee.setId(Long.parseLong(employeeFields[0]));
                            employee.setFirstName(employeeFields[1]);
                            employee.setLastName(employeeFields[2]);
                            employee.setEmail(employeeFields[3]);
                            message.append(updateEmployees(employee) ? "Updated" : "Error. Please try again");
                            try {
                                execute(new SendMessage().setChatId(update.getMessage().getChatId())
                                        .setText(message.toString()));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                try {
                    execute(sendInlineKeyBoardMessage(update.getMessage().getChatId()));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data.equals("LIST ALL")) {
                getAllEmployees();
                if (employees != null && !employees.isEmpty()) {
                    message.append("List of employees \n\n");
                    employees.forEach(e -> {
                        message.append(e.getId() + ". " + e.getFirstName() + " " + e.getLastName() + " " + e.getEmail())
                                .append("\n\n");
                    });
                } else {
                    message.append("No employees");
                }
            }
            if (data.equals("DELETE")) {
                actionsMap.put("DELETE", true);
                message.append("Enter the ID for deleting");
            }
            if (data.equals("ADD NEW")) {
                actionsMap.put("ADD NEW", true);
                message.append("Enter first name, last name and email.\nAs in the example:\n");
                message.append("Michael Zhebov michael@zhebov.com");
            }
            if (data.equals("UPDATE")) {
                actionsMap.put("UPDATE", true);
                message.append("Enter ID, first name last name and email.\nAs in the example:\n");
                message.append("21 Michael Zhebov michael@zhebov.com");
            }
            try {
                execute(new SendMessage().setText(
                        message.toString())
                        .setChatId(update.getCallbackQuery().getMessage().getChatId()));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public static SendMessage sendInlineKeyBoardMessage(long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        keyboardButtonsRow.add(new InlineKeyboardButton().setText("LIST ALL").setCallbackData("LIST ALL"));
        keyboardButtonsRow.add(new InlineKeyboardButton().setText("ADD NEW").setCallbackData("ADD NEW"));
        keyboardButtonsRow.add(new InlineKeyboardButton().setText("UPDATE").setCallbackData("UPDATE"));
        keyboardButtonsRow.add(new InlineKeyboardButton().setText("DELETE").setCallbackData("DELETE"));
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return new SendMessage().setChatId(chatId).setText("Choose the action").setReplyMarkup(inlineKeyboardMarkup);
    }

    @Override
    public String getBotUsername() {
        return System.getenv("username");
    }

    @Override
    public String getBotToken() {
        return System.getenv("token");
    }

    private void getAllEmployees() {
        Call<List<Employee>> callSync = networkService.getJSONApi().getAllEmployees();
        try {
            Response<List<Employee>> response = callSync.execute();
            employees = response.body();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean deleteEmployees(long id) {
        Call<Employee> callSync = networkService.getJSONApi().deleteEmployee(id);
        try {
            Response<Employee> response = callSync.execute();
            return response.body() != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean addEmployees(Employee employee) {
        Call<Employee> callSync = networkService.getJSONApi().addEmployee(employee);
        try {
            Response<Employee> response = callSync.execute();
            return response.body() != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateEmployees(Employee employee) {
        NetworkService networkService = NetworkService.getInstance();
        Call<Employee> callSync = networkService.getJSONApi().updateEmployee(employee.getId(),employee);
        try {
            Response<Employee> response = callSync.execute();
            return response.body() != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
