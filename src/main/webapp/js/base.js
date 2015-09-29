var google = google || {};
google.appengine = google.appengine || {};
google.appengine.timeclicker = google.appengine.timeclicker || {};

// client settings
google.appengine.timeclicker.CLIENT_ID = '599817378106-1n1ved0cprlim5chren2p0cj7lfjjdso.apps.googleusercontent.com';
google.appengine.timeclicker.SCOPES = 'https://www.googleapis.com/auth/userinfo.email';

/**
 * Whether or not the user is signed in.
 * @type {boolean}
 */
google.appengine.timeclicker.signedIn = false;

google.appengine.timeclicker.init = function(apiRoot) {
  // Loads the OAuth and helloworld APIs asynchronously, and triggers login
  // when they have completed.
  var apisToLoad;
  var callback = function() {
    if (--apisToLoad == 0) {
      console.log('API loaded');
      google.appengine.timeclicker.enableButtons();
      google.appengine.timeclicker.signin(true,
          google.appengine.timeclicker.userAuthed);
    }
  }

  apisToLoad = 2; // must match number of calls to gapi.client.load()
  gapi.client.load('timeclicker', 'v1', callback, apiRoot);
  gapi.client.load('oauth2', 'v2', callback);
};

/**
 * Handles the auth flow, with the given value for immediate mode.
 * @param {boolean} mode Whether or not to use immediate mode.
 * @param {Function} callback Callback to call on completion.
 */
google.appengine.timeclicker.signin = function(mode, callback) {
  gapi.auth.authorize({client_id: google.appengine.timeclicker.CLIENT_ID,
      scope: google.appengine.timeclicker.SCOPES, immediate: mode},
      callback);
};

/**
 * Enables the button callbacks in the UI.
 */
google.appengine.timeclicker.enableButtons = function() {
  var startButton = document.querySelector('#start');
  startButton.addEventListener('click', google.appengine.timeclicker.start);
  var stopButton = document.querySelector('#stop');
  stopButton.addEventListener('click', function(e) {
    google.appengine.timeclicker.stop();
  });
  var signinButton = document.querySelector('#signinButton');
  signinButton.addEventListener('click', google.appengine.timeclicker.auth);

  // enable "start" button by default
  google.appengine.timeclicker.enableButton(document.querySelector('#start'));
  google.appengine.timeclicker.disableButton(document.querySelector('#stop'));
};

/**
 * Presents the user with the authorization popup.
 */
google.appengine.timeclicker.auth = function() {
  if (!google.appengine.timeclicker.signedIn) {
    google.appengine.timeclicker.signin(false,
        google.appengine.timeclicker.userAuthed);
  } else {
    google.appengine.timeclicker.signedIn = false;
    document.querySelector('#signinButton').textContent = 'Sign in';
  }
};

/**
 * Loads the application UI after the user has completed auth.
 */
google.appengine.timeclicker.userAuthed = function() {
  var request = gapi.client.oauth2.userinfo.get().execute(function(resp) {
    if (!resp.code) {
      google.appengine.timeclicker.signedIn = true;
      document.querySelector('#signinButton').textContent = 'Sign out';
      // display message if already tracking
      google.appengine.timeclicker.latest();
      // calculate sum
      google.appengine.timeclicker.displaySum();
    }
  });
};

/**
 * Prints an entry to the entry log.
 * param {Object} entry Entry to print.
 */
google.appengine.timeclicker.print = function(entry) {
  var element = document.createElement('tr');
  //element.classList.add('row');
  element.innerHTML = '<td>' + entry.key + '</td><td>' + entry.start + '</td><td>' + entry.stop + '</td>';
  document.querySelector('#outputLog').appendChild(element);
};

/**
 * Prints the "currently active" message.
 */
google.appengine.timeclicker.printActive = function(entry) {
  var element = document.createElement('div');
  element.id = 'currentlyactive';
  element.classList.add('alert');
  element.classList.add('alert-info')
  element.innerHTML = 'You are already tracking since ' + entry.start;
  document.querySelector('#messages').appendChild(element);
};

/**
 * Removes the "currently active" message.
 */
google.appengine.timeclicker.removeActive = function() {
  var element = document.querySelector('#currentlyactive');
  if(element != undefined) {
    element.remove();
  }
};

/**
 * Removes all sum output.
 */
google.appengine.timeclicker.removeSum = function() {
  var elements = document.querySelector('#sumLog');
  for(var i = elements.children.length - 1;i >= 0;i--) {
    elements.children[i].remove();
  }
};

/**
 * Prints the overall sum to the sum field.
 * param {Object} TimeSum to print.
 */
google.appengine.timeclicker.printSum = function(prefix, entry) {
  var element = document.createElement('div');
  element.id = prefix.toLowerCase();
  //element.classList.add('row');
  element.innerHTML = prefix + ' ' + humanizeDuration(parseInt(entry.duration));
  document.querySelector('#sumLog').appendChild(element);
};

/**
 * Enable the given element.
 *
 * param {Node} DOM element
 */
google.appengine.timeclicker.enableButton = function(element) {
  element.classList.remove("hidden");
};

/**
 * Disable the given element.
 *
 * param {Node} DOM element
 */
google.appengine.timeclicker.disableButton = function(element) {
  element.classList.add("hidden");
};

/**
 * Lists entries via the API.
 */
google.appengine.timeclicker.listEntries = function() {
  gapi.client.timeclicker.list().execute(
      function(resp) {
        if (!resp.code) {
          resp.items = resp.items || [];
          for (var i = 0; i < resp.items.length; i++) {
            google.appengine.timeclicker.print(resp.items[i]);
          }
        }
      });
};

/**
 * Find latest open entry via the API.
 */
google.appengine.timeclicker.latest = function() {
  gapi.client.timeclicker.latest().execute(
      function(resp) {
        if (!resp.code && resp.start != undefined) {
          console.log("Latest found");
          console.log(resp.start);
          google.appengine.timeclicker.printActive(resp);
          // hide "start" button until "stop" button is clicked
          google.appengine.timeclicker.enableButton(document.querySelector('#stop'));
          google.appengine.timeclicker.disableButton(document.querySelector('#start'));
        } else {
          google.appengine.timeclicker.removeActive();
          // hide "stop" button
          google.appengine.timeclicker.enableButton(document.querySelector('#start'));
          google.appengine.timeclicker.disableButton(document.querySelector('#stop'));
        }
      });
};

/**
 * Start entry via the API.
 */
google.appengine.timeclicker.start = function() {
  gapi.client.timeclicker.start().execute(
      function(resp) {
        if (!resp.code) {
          console.log('Entry started');
          console.log(resp.start);
          google.appengine.timeclicker.latest();
          google.appengine.timeclicker.displaySum();
        }
      });
};

/**
 * Stop entry via the API.
 */
google.appengine.timeclicker.stop = function() {
  gapi.client.timeclicker.stopLatest().execute(
      function(resp) {
        if (!resp.code) {
          console.log('Entry stopped');
          console.log(resp.stop);
          google.appengine.timeclicker.latest();
          google.appengine.timeclicker.displaySum();
        }
      });
};

google.appengine.timeclicker.displaySum = function() {
  // remove old sum
  google.appengine.timeclicker.removeSum();
  // display current sum
  google.appengine.timeclicker.overallSum();
  google.appengine.timeclicker.monthlySum();
  google.appengine.timeclicker.weeklySum();
  google.appengine.timeclicker.dailySum();
};

/**
 * Calculate overall sum via the API.
 */
google.appengine.timeclicker.overallSum = function() {
  gapi.client.timeclicker.overallSum().execute(
      function(resp) {
        if (!resp.code) {
          google.appengine.timeclicker.printSum('Overall', resp);
        }
      });
};

/**
 * Calculate monthly sum via the API.
 */
google.appengine.timeclicker.monthlySum = function() {
  gapi.client.timeclicker.monthlySum().execute(
      function(resp) {
        if (!resp.code) {
          google.appengine.timeclicker.printSum('Monthly', resp);
        }
      });
};

/**
 * Calculate weekly sum via the API.
 */
google.appengine.timeclicker.weeklySum = function() {
  gapi.client.timeclicker.weeklySum().execute(
      function(resp) {
        if (!resp.code) {
          google.appengine.timeclicker.printSum('Weekly', resp);
        }
      });
};

/**
 * Calculate daily sum via the API.
 */
google.appengine.timeclicker.dailySum = function() {
  gapi.client.timeclicker.dailySum().execute(
      function(resp) {
        if (!resp.code) {
          google.appengine.timeclicker.printSum('Daily', resp);
        }
      });
};