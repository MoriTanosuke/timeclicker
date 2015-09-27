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
      // load entries
      google.appengine.timeclicker.listEntries();
      // calculate sum
      google.appengine.timeclicker.overallSum();
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
 * Prints the overall sum to the sum field.
 * param {Object} TimeSum to print.
 */
google.appengine.timeclicker.printSum = function(prefix, entry) {
  var element = document.createElement('div');
  //element.classList.add('row');
  element.innerHTML = prefix + ' ' + humanizeDuration(parseInt(entry.duration));
  document.querySelector('#sumLog').appendChild(element);
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
 * Start entry via the API.
 */
google.appengine.timeclicker.start = function() {
  gapi.client.timeclicker.start().execute(
      function(resp) {
        if (!resp.code) {
          resp.items = resp.items || [];
          for (var i = 0; i < resp.items.length; i++) {
            google.appengine.timeclicker.print(resp.items[i]);
            //TODO add key of last started entry
            document.querySelector('#key').value = resp.items[i].key;
          }
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
          resp.items = resp.items || [];
          for (var i = 0; i < resp.items.length; i++) {
            google.appengine.timeclicker.print(resp.items[i]);
          }
        }
      });
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