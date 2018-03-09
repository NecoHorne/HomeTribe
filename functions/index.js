let functions = require('firebase-functions');
let admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.incidentNotification = functions.database.ref('/incidents/{key_ref}').onWrite(event => {

    // check to see if the event is a delete or a modification
    if (!event.data.val() || event.data.previous.val()) {
        return;
    }

    //get incident type, location and description from write event
    const incidentLat = event.data.child('incident_location').child('latitude').val()
    const incidentLng = event.data.child('incident_location').child('longitude').val()
    const incidentType = event.data.child('incident_type').val();
    const incidentDescription = event.data.child('incident_description').val();
    const incidentReportedBy = event.data.child('reported_by').val();
    const incidentReference = event.params.key_ref;

    var tokens = [];

    //get a list of all the users in the users node.
    return event.data.ref.parent.parent.once('value').then(snap => {
    		var data = snap.child('users').val();

    		for(var user_id in data){

                    //get user home location.
                    var homeLat = snap.child('users').child(user_id).child('home_location').child('latitude').val();
                    var homeLng = snap.child('users').child(user_id).child('home_location').child('longitude').val();

                    //check to see if the incident reported_by and the userId is the same, if that's the case do not send notification to that user.
                    if(user_id !== incidentReportedBy){
                       //calculate distance between incident and user home in Km.
                        var distance = getDistance(homeLat, homeLng, incidentLat, incidentLng)/1000;

                        //if calculated distance is less than or equal to 5km get users fcm token and add to tokens array.
                        if(distance <= 5){
                            var token = snap.child('users').child(user_id).child('/fcm_token').val();
                            tokens.push(token);
                        }
                    }
            }

            //construct payload to send to client device
            const payload = {
                data:{
                    data_type: "data_type_incident",
                    title: incidentType,
                    description: incidentDescription,
                    reference: incidentReference
                }
            }

            if(tokens.length > 0){
                //send the payload to all relevant user devices.
                return admin.messaging().sendToDevice(tokens, payload).then(function(response) {
                return console.log("Successfully sent CM:", response);
                }).catch(function(error) {
                console.log("Error sending CM:", error);
                });
            }
            return;
    });
});


//function for checking distance between 2 LatLng objects.

var rad = function(x) {
  return x * Math.PI / 180;
};

var getDistance = function(lat1, lng1, lat2, lng2) {
    var R = 6378137; // Earth’s mean radius in meter
    var dLat = rad(lat2 - lat1);
    var dLong = rad( lng2 - lng1);
    var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(rad(lat1)) * Math.cos(rad(lat2)) *
        Math.sin(dLong / 2) * Math.sin(dLong / 2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    var d = R * c;
    return d; // returns the distance in meter
    };