// imports firebase-functions module
const functions = require('firebase-functions');
// imports firebase-admin module
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

/* Listens for new messages added to /messages/:pushId and sends a notification to subscribed users */
exports.pushNotification = functions.database.ref('/messages/{pushId}').onWrite( async ( change,context) => {
console.log('Push notification event triggered');
/* Grab the current value of what was written to the Realtime Database */
    var valueObject = change.after.val();
/* Create a notification and data payload. They contain the notification information, and message to be sent respectively */ 
    const payload = {
        notification: {
            title: 'Event_moi_ça',
            body: "Une photo a été signalée sur l'un de vos évènements !",
            sound: "default"
        },
        data: {
            title: valueObject.title,
            eventOwner: valueObject.eventOwner,
            eventId: valueObject.eventId
        }
    };
/* Create an options object that contains the time to live for the notification and the priority. */
    const options = {
        priority: "high",
        timeToLive: 60 * 60 * 24 //24 hours
    };
    console.log(valueObject.eventOwner);
    const getDeviceTokenPromise = admin.database().ref(`/tokens/${valueObject.eventOwner}`).once('value');
    const results = await Promise.all([getDeviceTokenPromise]);
    tokensSnapshot = results[0];
    tokens = tokensSnapshot.val();
    const response = await admin.messaging().sendToDevice(tokens, payload);
    console.log("tokens : ", tokens)
    const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove);

    //return admin.messaging().sendToTopic(`${valueObject.eventOwner}`, payload, options);
}); 

