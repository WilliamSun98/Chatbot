describe('Protractor Demo App', function() {
  beforeAll(function() {
    browser.get('http://localhost:4200/login');
  })

  it('init', function() {
    // Check page title
    expect(browser.getTitle()).toEqual('FrontEnd');
    // Current page should be the initial login page
    expect(element(by.tagName('app-login')).isPresent()).toBe(true);
  });

  it('proceed', function() {
    element(by.className('guest-link')).click();
    expect(browser.getTitle()).toEqual('FrontEnd');
    // Current page should be the chatting main page
    expect(element(by.tagName('app-chatbot')).isPresent()).toBe(true);
  });

  it('check partnership', function() {
    expect(element(by.className('modal-open')).isPresent()).toBe(false);
    element(by.className('fas fa-handshake fa-2x icon-hover')).click();
    expect(element(by.className('modal-open')).isPresent()).toBe(true);
  });

  it('close partnership', function() {
    expect(element(by.className('modal-open')).isPresent()).toBe(true);
    element(by.className('fas fa-times fa-md icon-hover')).click();
    expect(element(by.className('modal-open')).isPresent()).toBe(false);
  });

  it('check info', function() {
    element(by.className('fas fa-info-circle fa-2x')).click();
    expect(element(by.className('tooltip-inner')).isPresent()).toBe(true);
  });

  it('open filter', function() {
    expect(element(by.className('filter-container')).isPresent()).toBe(false);
    element(by.className('fas fa-filter fa-lg')).click();
    expect(element(by.className('filter-container')).isPresent()).toBe(true);
  });

  it('close filter', function() {
    expect(element(by.className('filter-container')).isPresent()).toBe(true);
    element(by.className('fas fa-filter fa-lg')).click();
    expect(element(by.className('filter-container')).isPresent()).toBe(false);
  });

  it('type input1', function() {
    expect(element(by.className('message')).isPresent()).toBe(false);
    element(by.className('form-control ng-untouched ng-pristine ng-invalid')).sendKeys('statement1');
    element(by.className('btn btn-chatbot')).click();
    expect(element(by.className('message')).getText()).toBe('statement1');
  });

  // it('change color', function() {
  //   expect(element(by.className('message')).getAttribute('style')).toBe('background-color: rgb(153, 225, 217);');
  //   element(by.className('fas fa-palette fa-2x icon-hover')).click();
  //   var ul = document.getElementById("foo");
  //   var items = ul.getElementsByTagName("li");
  //   for (var i = 0; i < items.length; ++i) {
  //     // do something with items[i], which is a <li> element
  //   }
  //
  //   var row2 = document.getElementsByClassName('row'); //element.all(by.className('row')).get(1);
  //   var child = row2.all(by.className('dot')).get(1);
  //   child.click();
  //   expect(element(by.className('message')).getAttribute('style')).toBe('background-color: rgb(227, 218, 255);');
  //   // expect(element(by.className('message')).getText()).toBe('statement1');
  // });

});
