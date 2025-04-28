const AudioRecorder = {
  recorder: null,
  chunks: [],
  result: null,
  stream: null,

  async start() {
    if (this.recorder && this.recorder.state === "recording") {
      console.log("Already recording");
      return;
    }

    this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    this.recorder = new MediaRecorder(this.stream);
    this.chunks = [];

    this.recorder.ondataavailable = e => this.chunks.push(e.data);
    this.recorder.onstop = () => {
      const blob = new Blob(this.chunks, { type: 'audio/webm' });
      const reader = new FileReader();
      reader.onloadend = () => {
        this.result = reader.result;
      };
      reader.readAsDataURL(blob);

      this.stream.getTracks().forEach(track => track.stop());
    };

    this.recorder.start();
  },

  stop() {
    if (this.recorder && this.recorder.state === "recording") {
      this.recorder.stop();
    } else {
      console.warn("Recorder is not ready or already stopped.");
    }
  },
};

window.AudioRecorder = AudioRecorder;
