export class Message {
  public message: String;
  public time: Date;
  public bot: Boolean;

  constructor(message: String, time: Date, bot: Boolean) {
    this.message = message;
    this.time = time;
    this.bot = bot;
  }
}
