package com.telega.bot;

import com.telega.bot.model.Employee;
import com.telega.bot.service.NetworkService;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public class BotApplication extends TelegramLongPollingBot {

    private List<Employee> employees;

    @Override
    public void onUpdateReceived(Update update) {
        String message = update.getMessage().getText();
        StringBuilder sbMsg = new StringBuilder();
        if (message.equals("/start")) {
            sbMsg.append("Hello " + update.getMessage().getFrom().getFirstName() + "! I'm a bot!")
                    .append("\nPlease use command: \n'/list'\n for get a list of employees from data base.");
            sendMsg(update.getMessage().getChatId().toString(), sbMsg.toString());
        } else if (message.equals("/list")) {
            StringBuilder list = new StringBuilder();
            getAllEmployees();
            if (employees != null && !employees.isEmpty()) {
                list.append("Employees list: \n\n");
                employees.forEach(e -> {
                    list.append(e.getFirstName() + " " + e.getLastName() + " " + e.getEmail())
                            .append("\n\n");
                });
            } else {
                list.append("No employees");
            }
            sendMsg(update.getMessage().getChatId().toString(), list.toString());
        } else {
            sbMsg.append("\nPlease use command: \n'/list'\n for get a list of employees from data base.");
            sendMsg(update.getMessage().getChatId().toString(), sbMsg.toString());
        }
    }

    public synchronized void sendMsg(String chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            //log.log(Level.SEVERE, "Exception: ", e.toString());
        }
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
        NetworkService networkService = NetworkService.getInstance();
        Call<List<Employee>> callSync = networkService.getJSONApi().getAllEmployees();

        try {
            Response<List<Employee>> response = callSync.execute();
            employees = response.body();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
