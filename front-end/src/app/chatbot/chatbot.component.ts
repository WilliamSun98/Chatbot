import { HttpClient } from '@angular/common/http';
import {
  AfterViewChecked,
  Component,
  ElementRef,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Message } from './message.model';

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent implements OnInit, AfterViewChecked {
  @ViewChild('chatbox') chatbox: ElementRef;
  @ViewChild('siteInput') siteInput: ElementRef;
  @ViewChild('startDateInput') startDateInput: ElementRef;
  @ViewChild('endDateInput') endDateInput: ElementRef;

  messageColour = '#99e1d9';
  partnersModal: BsModalRef;
  uploadFileModal: BsModalRef;
  queryForm: FormGroup;
  messages: Message[] = [];
  greeting = 'Hello. How can I help you?';
  infoMessage =
    'This chatbot provides answers to questions about FinTech, AI, innovation, investment, and Canada.';
  folded = true;
  isjob = false;
  pdf = false;

  constructor(private httpClient: HttpClient, private modalService: BsModalService) {}

  ngOnInit() {
    const newMessage = new Message(this.greeting, new Date(), true);
    this.messages.push(newMessage);
    this.queryForm = new FormGroup({
      query: new FormControl('', Validators.required)
    });
    this.scrollToBottom();
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  setColour(colour: string) {
    this.messageColour = colour;
  }

  openModal(template: TemplateRef<any>) {
    this.partnersModal = this.modalService.show(template, Object.assign({}, { class: 'modal-xl' }));
  }

  openUploadModal(template: TemplateRef<any>) {
    this.uploadFileModal = this.modalService.show(
      template,
      Object.assign({}, { class: 'modal-md' })
    );
  }

  onSubmit() {
    if (this.queryForm.value.query) {
      const newMessage = new Message(this.queryForm.value.query, new Date(), false);
      this.messages.push(newMessage);
    }
    this.getResponse(this.queryForm.value.query);
    this.queryForm.reset();
    this.scrollToBottom();
  }

  scrollToBottom() {
    try {
      this.chatbox.nativeElement.scrollTop = this.chatbox.nativeElement.scrollHeight;
    } catch (err) {}
  }

  getResponse(query: String) {
    const formData = new FormData();
    formData.append('input', query as USVString);
    formData.append('site', this.siteInput ? this.siteInput.nativeElement.value : null);
    formData.append(
      'startDate',
      this.startDateInput ? this.startDateInput.nativeElement.value : null
    );
    formData.append('endDate', this.endDateInput ? this.endDateInput.nativeElement.value : null);
    formData.append('pdf', this.pdf ? 'true' : 'false');
    formData.append('isjob', this.isjob ? 'true' : 'false');

    this.httpClient.post('http://localhost:8080/get-answer', formData).subscribe(
      res => {
        if (res['watson'] != null) {
          const newMessage = new Message(res['watson'], new Date(), true);
          this.messages.push(newMessage);
        } else if (res['jobs'] != null) {
          for (const job of res['jobs']) {
            const newMessage = new Message(
              '<a class="bot-link" href="' +
                job['url'] +
                '" target="_blank">' +
                job['title'] +
                ' at ' +
                job['company'] +
                '</a>',
              new Date(),
              true
            );
            this.messages.push(newMessage);
          }
        } else if (res['files'] != null && res['files'].length > 0) {
          for (const file of res['files']) {
            if (file['url'].startsWith('http')) {
              const newMessage = new Message(
                '<a class="bot-link" href="' +
                  file['url'] +
                  '" target="_blank">' +
                  file['title'] +
                  '</a>',
                new Date(),
                true
              );
              this.messages.push(newMessage);
            } else {
              const name1 = file['url'].split('/');
              const name = name1[name1.length - 1];
              const extns = name.split('.');
              const extn = extns[extns.length - 1];
              const newLink = 'http://localhost:8080/get-pdf?extn=' + extn + '&path=' + file['url'];
              const newMessage = new Message(
                '<a class="bot-link" href="' +
                  newLink +
                  '" target="_blank">' +
                  file['title'] +
                  '</a>',
                new Date(),
                true
              );
              this.messages.push(newMessage);
            }
          }
        } else if (this.siteInput.nativeElement.value !== '') {
          const newMessage = new Message('Crawling...', new Date(), true);
          this.messages.push(newMessage);
        } else {
          const newMessage = new Message('We found nothing :(', new Date(), true);
          this.messages.push(newMessage);
        }
      },
      error => {
        console.log(error);
      }
    );
  }

  hoverOver() {
    this.folded = !this.folded;
  }

  hintCheck(event) {
    if (event.target.checked) {
      const newMessage = new Message(
        'Since you want to search by job, please enter the job type below.',
        new Date(),
        true
      );
      this.messages.push(newMessage);
    } else {
      const newMessage = new Message(
        "Since you don't want to search by job, feel free to ask anything you want.",
        new Date(),
        true
      );
      this.messages.push(newMessage);
    }
  }

  handleFileInput(files: FileList) {
    if (files.item(0).size >= 1048576) {
      const newMessage = new Message('File exceeds the 1MB size limit.', new Date(), true);
      this.messages.push(newMessage);
    } else {
      const endpoint = 'http://localhost:8080/upload-file';
      const formData: FormData = new FormData();
      formData.append('file', files.item(0));

      this.httpClient.post(endpoint, formData, { responseType: 'text' }).subscribe(
        res => {
          const newMessage = new Message('File upload successful.', new Date(), true);
          this.messages.push(newMessage);
        },
        error => {
          console.log(error);
          const newMessage = new Message('File upload failure.', new Date(), true);
          this.messages.push(newMessage);
        }
      );
    }
  }

  goToLink(url: string) {
    window.open(url, '_blank');
  }
}
