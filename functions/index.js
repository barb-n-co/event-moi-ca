// imports firebase-functions module
const functions = require('firebase-functions');
// imports firebase-admin module
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

/* Listens for new messages added to /messages/:pushId and sends a notification to subscribed users */
exports.pushNotification = functions.database.ref('/messages/{pushId}').onWrite( ( change,context) => {
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
            message: valueObject.message
        }
    };
/* Create an options object that contains the time to live for the notification and the priority. */
    const options = {
        priority: "high",
        timeToLive: 60 * 60 * 24 //24 hours
    };

    const channelId = valueObject.message.toString()
return admin.messaging().sendToTopic("notif_event_moi_ca", payload, options);
}); 

