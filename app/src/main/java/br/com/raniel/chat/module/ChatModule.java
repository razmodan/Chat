package br.com.raniel.chat.module;

import android.app.Application;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import br.com.raniel.chat.service.ChatService;
import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ChatModule {
    private Application app;

    public ChatModule(Application app) {
        this.app = app;
    }

    @Provides
    public ChatService getChatService(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.14:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ChatService chatService = retrofit.create(ChatService.class);

        return chatService;
    }

    @Provides
    public EventBus getEvenBus(){
        return EventBus.builder().build();
    }

    @Provides
    public Picasso picaso(){
        Picasso picasso = new Picasso.Builder(app).build();
        return picasso;
    }

    @Provides
    public InputMethodManager getInputMethodManager(){
        InputMethodManager inputMethodManager = (InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE);

        return inputMethodManager;
    }
}
