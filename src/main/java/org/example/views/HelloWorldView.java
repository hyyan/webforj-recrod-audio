package org.example.views;

import static com.webforj.component.optiondialog.OptionDialog.showMessageDialog;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;

import com.webforj.Page;
import com.webforj.annotation.JavaScript;
import com.webforj.component.Composite;
import com.webforj.component.Theme;
import com.webforj.component.button.Button;
import com.webforj.component.button.ButtonTheme;
import com.webforj.component.element.Element;
import com.webforj.component.icons.TablerIcon;
import com.webforj.component.layout.flexlayout.FlexDirection;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.toast.Toast;
import com.webforj.router.annotation.Route;

@Route("/")
@JavaScript("ws://AudioRecorder.js")
public class HelloWorldView extends Composite<FlexLayout> {

  private FlexLayout self = getBoundComponent();
  private Button recordButton = new Button("Record", ButtonTheme.PRIMARY);
  private Button uploadButton = new Button("Upload", ButtonTheme.GRAY); // New upload button
  Element audio = new Element("audio");
  private boolean isRecording = false;

  public HelloWorldView() {
    self.setDirection(FlexDirection.COLUMN);
    self.setMaxWidth(300);
    self.setStyle("margin", "1em auto");

    Page page = Page.getCurrent();

    audio.setAttribute("controls", "");
    audio.setVisible(false);

    recordButton.setPrefixComponent(TablerIcon.create("microphone"));
    recordButton.onClick(e -> {
      if (isRecording) {
        recordButton.setText("Record");
        recordButton.setPrefixComponent(TablerIcon.create("microphone"));
        isRecording = false;
        page.executeJs("window.AudioRecorder.stop()");

        audio.setAttribute("src", String.valueOf(
            page.executeJs("window.AudioRecorder.result")));
        audio.setVisible(true);
        uploadButton.setVisible(true);
      } else {
        recordButton.setText("Stop");
        recordButton.setPrefixComponent(TablerIcon.create("player-stop"));
        isRecording = true;
        page.executeJsVoidAsync("window.AudioRecorder.start()");
        audio.setVisible(false);
        uploadButton.setVisible(false);
      }
    });

    uploadButton
        .setPrefixComponent(TablerIcon.create("upload"))
        .setVisible(false)
        .onClick(e -> {
          String base64Audio = String.valueOf(page.executeJs("window.AudioRecorder.result"));
          try {
            String filePath = System.getProperty("java.io.tmpdir") + "/audio.webm";
            upload(base64Audio, filePath);
            showMessageDialog("Audio file saved to: " + filePath, "File saved");

          } catch (Exception ex) {
            Toast.show("Error uploading audio: " + ex.getMessage(), Theme.DANGER);
          }
        });

    self.add(recordButton, audio, uploadButton);
  }

  public void upload(String base64Audio, String filePath) throws Exception {
    // Remove the prefix
    String[] parts = base64Audio.split(",");
    String base64Data = parts.length > 1 ? parts[1] : parts[0];

    byte[] audioBytes = Base64.getDecoder().decode(base64Data);

    try (OutputStream out = new FileOutputStream(filePath)) {
      out.write(audioBytes);
    }
  }
}
