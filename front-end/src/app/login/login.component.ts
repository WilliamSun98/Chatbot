import {AfterViewInit, Component } from '@angular/core';

declare const gapi: any;

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements AfterViewInit {

  private clientId:string = '584632631615-fvl80a72321k67t7urbash1ihvce5tq7.apps.googleusercontent.com';

  constructor() { }

  private scope = [
    'profile',
    'email',
    'https://www.googleapis.com/auth/plus.me',
    'https://www.googleapis.com/auth/contacts.readonly',
    'https://www.googleapis.com/auth/admin.directory.user.readonly'
  ].join(' ');

  public auth2: any;

  loggedin = false;
  currUser = "Nobody";
  profile;

  ngAfterViewInit() {
    let that = this;
    gapi.load('auth2', function () {
      that.auth2 = gapi.auth2.init({
        client_id: that.clientId,
        cookiepolicy: 'single_host_origin',
        scope: that.scope
      });
      that.attachSignin(document.getElementById('googleBtn'));
    });
  }

  public attachSignin(element) {
    this.auth2.attachClickHandler(element, {},
      (googleUser) => {
        this.profile = googleUser.getBasicProfile();
        this.currUser = this.profile.getName();
        document.getElementById('loggedBtn').innerText = "Continue as " + this.currUser;
      }, function (error) {
        console.log(JSON.stringify(error, undefined, 2));
      });
  }

}
