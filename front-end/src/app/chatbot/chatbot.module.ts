import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';
import { ModalModule } from 'ngx-bootstrap/modal';
import { PopoverModule } from 'ngx-bootstrap/popover';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { ChatbotRoutingModule } from './chatbot-routing.module';
import { ChatbotComponent } from './chatbot.component';
import { SafeHtml } from './chatbot.pipe';
import {Ng2FileSizeModule} from 'ng2-file-size';
import { UploaderModule } from '@syncfusion/ej2-angular-inputs';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    ChatbotRoutingModule,
    ReactiveFormsModule,
    TooltipModule.forRoot(),
    BsDatepickerModule.forRoot(),
    PopoverModule.forRoot(),
    FormsModule,
    ModalModule.forRoot(),
    Ng2FileSizeModule,
    UploaderModule,
  ],
  declarations: [ChatbotComponent, SafeHtml]
})
export class ChatbotModule {}
