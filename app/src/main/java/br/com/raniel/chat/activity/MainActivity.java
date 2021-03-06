package br.com.raniel.chat.activity;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import br.com.raniel.chat.R;
import br.com.raniel.chat.adapter.MensagemAdapter;
import br.com.raniel.chat.app.ChatApplication;
import br.com.raniel.chat.callback.EnviarMensagemCallback;
import br.com.raniel.chat.callback.OuvirMensagemCallBack;
import br.com.raniel.chat.component.ChatComponent;
import br.com.raniel.chat.event.MensagemEvent;
import br.com.raniel.chat.event.MessageFailureEvent;
import br.com.raniel.chat.model.Mensagem;
import br.com.raniel.chat.service.ChatService;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_mensagem)
    EditText campoConteudoMensagem;
    @BindView(R.id.main_enviar)
    Button botaoEnviar;
    @BindView(R.id.main_listview_conversa)
    ListView lvListaDeMensagens;
    @BindView(R.id.main_avatar_usuario)
    ImageView avatar;

    private int idDoUsuario = 2;
    private List<Mensagem> mensagens;

    @Inject
    public ChatService chatService;
    @Inject
    public Picasso picasso;
    @Inject
    public EventBus eventbus;
    @Inject
    public InputMethodManager inputMethodManager;

    private ChatComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if (savedInstanceState != null)
            mensagens = savedInstanceState.getParcelableArrayList("mensagem");
        else
            mensagens = new ArrayList<>();

        MensagemAdapter mensagemAdapter = new MensagemAdapter(this, mensagens, idDoUsuario);
        lvListaDeMensagens.setAdapter(mensagemAdapter);

        ChatApplication app = (ChatApplication) getApplication();
        component = app.getComponent();
        component.inject(this);

        picasso.get().load("https://api.adorable.io/avatars/285/" + idDoUsuario + ".png").into(avatar);

        eventbus.register(this);
        ouvirMensagem(null);
    }

    @Override
    protected void onStop() {
        super.onStop();

        eventbus.unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("mensagem", (ArrayList<Mensagem>) mensagens);
    }

    @OnClick(R.id.main_enviar)
    public void enviarMensagem() {
        Mensagem msg = new Mensagem(campoConteudoMensagem.getText().toString(), idDoUsuario);
        chatService.enviar(msg).enqueue(new EnviarMensagemCallback());

        campoConteudoMensagem.getText().clear();
        //InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(campoConteudoMensagem.getWindowToken(), 0);
    }

    @Subscribe
    public void colocaNaLista(MensagemEvent mensagemEvent) {
        mensagens.add(mensagemEvent.mensagem);
        MensagemAdapter adapter = new MensagemAdapter(this, mensagens, idDoUsuario);
        lvListaDeMensagens.setAdapter(adapter);
    }

    @Subscribe
    public void ouvirMensagem(MensagemEvent mensagemEvent) {
        Call<Mensagem> mensagemCall = chatService.ouvirMensagens();
        mensagemCall.enqueue(new OuvirMensagemCallBack(eventbus));
    }

    @Subscribe
    public void lidarCom(MessageFailureEvent event) {
        ouvirMensagem(null);
    }
}
