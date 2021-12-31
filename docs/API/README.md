# API Reference

The Oikos API is organized around REST.

Our API has predictable resource-oriented URLs, accepts JSON request bodies, returns
JSON-encoded responses, and uses standard HTTP response codes, authentication, and verbs.
***
# Table of Content
- [API Reference](#api-reference)
- [Table of Content](#table-of-content)
- [Authentication](#authentication)
- [Errors](#errors)
  - [Error Response](#error-response)
  - [HTTP status codes used](#http-status-codes-used)
- [Pagination](#pagination)
- [Versioning](#versioning)
- [Core Resources](#core-resources)
  - [DoneResponse](#doneresponse)
  - [Messages](#messages)
    - [Message object](#message-object)
    - [Message Thread Object](#message-thread-object)
    - [Message endpoints](#message-endpoints)
      - [Send a new message](#send-a-new-message)
      - [Get the message threads for the current user](#get-the-message-threads-for-the-current-user)
      - [Get the messages in a specific thread](#get-the-messages-in-a-specific-thread)
      - [Mark a message as read](#mark-a-message-as-read)
      - [Delete a message](#delete-a-message)
      - [Delete a message thread](#delete-a-message-thread)
  - [Notifications](#notifications)
    - [Notification object](#notification-object)
    - [Notification endpoints](#notification-endpoints)
      - [Get the notifications for the current user](#get-the-notifications-for-the-current-user)
      - [Get the unread notifications for the current user](#get-the-unread-notifications-for-the-current-user)
      - [Mark a notification as read](#mark-a-notification-as-read)
      - [Delete a notification](#delete-a-notification)
      - [Create a new notification](#create-a-new-notification)
  - [Security](#security)
    - [Signup](#signup)
    - [Signin](#signin)
    - [Refresh Token](#refresh-token)
    - [Mail confirmation](#mail-confirmation)
    - [Resend Mail confirmation](#resend-mail-confirmation)
    - [SMS confirmation](#sms-confirmation)
    - [Password forgotten](#password-forgotten)
    - [Password forgotten](#password-forgotten-1)
  - [Owner](#owner)
  - [Owner Object](#owner-object)
    - [Owner endpoints](#owner-endpoints)
      - [Get the current logged in owner info](#get-the-current-logged-in-owner-info)
      - [Get all the owners](#get-all-the-owners)
      - [Get an owner](#get-an-owner)
      - [Update the current logged in owner](#update-the-current-logged-in-owner)
      - [Update an owner](#update-an-owner)
  - [Property](#property)
    - [Property Object](#property-object)
      - [Create a new Property](#create-a-new-property)
      - [Get all the properties](#get-all-the-properties)
      - [Get all the properties by owner](#get-all-the-properties-by-owner)
      - [Get all the properties followed by a buyer](#get-all-the-properties-followed-by-a-buyer)
      - [Get a specific property](#get-a-specific-property)
      - [Modify property informations](#modify-property-informations)
      - [Delete a property](#delete-a-property)
      - [Set a property status to approved](#set-a-property-status-to-approved)
      - [Follow the changes of a specific property](#follow-the-changes-of-a-specific-property)
      - [Download a pdf recap of a property](#download-a-pdf-recap-of-a-property)
  - [Disponibility](#disponibility)
    - [Disponibility object](#disponibility-object)
    - [Disponibility endpoints](#disponibility-endpoints)
      - [Create a new disponibility](#create-a-new-disponibility)
      - [Delete disponibility](#delete-disponibility)
      - [Update disponibility](#update-disponibility)
    - [user-Disponibility object](#user-disponibility-object)
    - [user-Disponibility endpoints](#user-disponibility-endpoints)
      - [Assign disponibility to User](#assign-disponibility-to-user)
      - [delete user disponibility](#delete-user-disponibility)
      - [Update user disponibility](#update-user-disponibility)
      - [Request meeting](#request-meeting)
      - [cancel meeting request](#cancel-meeting-request)
      - [Accept meeting request](#accept-meeting-request)
      - [Refuse meeting request](#refuse-meeting-request)
  - [Visit Reviews](#visit-reviews)
    - [VisitReview Object](#visitreview-object)
    - [VisitReview Endpoints](#visitreview-endpoints)
      - [Create a new Review](#create-a-new-review)
      - [View the reviews by property](#view-the-reviews-by-property)
  - [Offers](#offers)
    - [Offer Object](#offer-object)
      - [Create a new offer](#create-a-new-offer)
      - [Consult offers for the current owner](#consult-offers-for-the-current-owner)
      - [Consult offers for a specific owner](#consult-offers-for-a-specific-owner)
      - [Consult offers for a specific property](#consult-offers-for-a-specific-property)
      - [Get the counteroffers of an offer](#get-the-counteroffers-of-an-offer)
      - [Accept an offer](#accept-an-offer)
      - [Refuse an offer](#refuse-an-offer)
 - [adverts](#adverts)
    - [adverts Object](#adverts-object)
        - [Create a new advert](#create-a-new-advert)
        - [Modify advert](#modify-advert)
        - [Delete advert](#Delete-advert)
        - [Get all adverts](#get-all-adverts)
        - [Get my adverts](#get-my-adverts)
        - [Get a specific advert](#get-a-specific-advert)
        - [Get advert by type](#get-advert-by-type)
        - [Set advert interested](#set-advert-interested)
 - [visits](#visits)
    - [visits Object](#visits-object)
        - [Create a new virtual visit](#create-a-new-virtual-visit)
        - [Create a new physical visit](#create-a-new-physical-visit)
        - [modify a visit](modify-a-visit)
        - [Delete a visit](#Delete-a-visit)
        - [Get all visits](#get-all-visits)
        - [Get specific visit](#get-specific-visit)
        - [Get specific type of visits](#get-specific-type-of-visits)
        - [Get pending visits](#get-pending-visits)
        - [Approve pending visits](#approve-pending-visits)
        - [Reject pending visits](#reject-pending-visits)
        - [Approve and suggest another timing](#approve-and-suggest-another-timing)
 - [services](#services)
      - [services Object](#services-object)
       - [Create a new service](#create-a-new-service)
       - [modify a service](modify-a-service)
       - [Delete a service](#Delete-a-visit)
       - [Get all services](#get-all-services)
 - [service-request](#service-request)
  - [service request Object](#services-request-object)
       - [Request a service](#request-a-service)
       - [Approve a service request](#approve-a-service-request)
       - [Reject a service request](#reject-a-service-request)
       - [modify a service request](#modify-a-service-request)
       - [Delete a service request](#delete-a-service-request)
       - [Get a specific service request](#get-a-specific-service-request)
       - [Get all service request](#get-all-service-request)
       - [Get pending service request](#get-pending-service-request)
 - [rating](#rating)
   - [rating Object](#rating-object)
      - [create a new rating ](#request-a-service)
      - [modify a rating](#modify-a-rating)
      - [Delete a rating](#delete-a-rating)
      - [Get all rating](#get-all-rating)
      - [Get a specific rating](#get-a-specific-rating)
-[refund-request](#refund-request)
   - [refund request Object](#refund-request-object)
     - [Create a new refund request](#create-a-new-refund-request)
     - [Get all refund request](#get-all-refund-request)
     - [Get a specific refund request](#Get-a-specific-refund-service)
     - [Approve a refund request](#approve-a-refund-request)
     - [Reject a refund request](#reject-a-refund-request)

***
# Authentication
Only the security endpoints don't require authentication. Everything else requires a connected user.

The authentication portion isn't done yet so while in test mode we connect using basic http auth
using a username and password.

The completed auth should use Oauth2 and JWT tokens as auth method instead.
***
# Errors

## Error Response

```json
{
    "apierror": {
        "status": "BAD_REQUEST",
        "timestamp": "27-02-2021 03:04:54",
        "message": "Error Message",
        "subErrors": null
    }
}
```
In the future the error messages should also contain a link to the documentation site that contains a more detailed explanation
## HTTP status codes used

* 200 - OK Everything worked as expected.
* 400 - Bad Request The request was unacceptable, often due to missing a required parameter.
* 401 - Unauthorized No valid Authentication provided.
* 403 - Forbidden The Authenticated user doesn't have permissions to perform the request.
* 404 - Not Found The requested resource doesn't exist.
* 500 - Server Error
***
# Pagination

We use the standard Spring pagination for now, so we have some redundant information in the response
Json but everything here is subject to change. Here is the standard paginated response :

```yaml
PagedResponse {

  totalElements integer

  totalPages integer

  size integer

  content	[]

  number integer

  sort Sort #useless

  first boolean

  last boolean

  numberOfElements integer

  pageable Pageable #useless

  empty boolean

}
```

The key properties are :

* **totalElements**: The total number of record matching the current request in the database.
* **totalPages**: The number of pages available with the current page size
* **size**: The current page size
* **content**: An array of the matching entities conforming to the request
* **number**: The current page's number
* **first**: Is this the first page
* **last**: Is this the last page
* **numberOfElements**: How many elements are there in the current page
* **empty**: Is the current page empty

Now to use the pagination on a request we use query params on a request that supports pagination in
the following way:

**page**: The requested page. If none specified it will default to 0 as that's the first page.
**size**: The size of the requested page. If none specified it will default to 10.
***
# Versioning

This is version 1. To ensure that if in the future we consider reworking the API we can do so without breaking the current clients we will prepend the API version to the endpoint.

So the full path of any request should be

```http
http://serverpath/api/v1/YOURENDPOINTHERE
```
***
# Core Resources
## DoneResponse
Returned when a request doesn't return an entity but instead perform an action

Example :
```json
{
    "message": "Message X2UkRb65mxqg has been marked as Read"
}
```
## Messages

### Message object

```properties
{
    id string
    senderId string
    recipientId string
    threadId string
    status [SENT or READ]
    dateModified datetime
    dateCreation datetime
    content string
}
```

### Message Thread Object

```properties
{
    id string
    recipientID string
    dateLastMessage date-time
}
```

### Message endpoints
#### Send a new message
```http request
POST /messages
```
Has no parameters. Returns the newly created [message object](#message-object). Requires the following request body format:

```json
{
    "recipientId": "string",
    "content": "string"
}
```
#### Get the message threads for the current user
```http request
GET /messages/threads
```
Paginated, for query parameters see the [Pagination section](#pagination). Returns [message threads](#message-thread-object)
for the current user sorted by the date of last message in a thread.
#### Get the messages in a specific thread
```http request
GET /messages/threads/:threadID
```
threadID is the id of the thread to see.

Paginated, for query parameters see the [Pagination section](#pagination).
Returns [messages](#message-object) in the selected thread sorted by the sending date.
#### Mark a message as read
```http request
GET /messages/:messageID/read
```
messageID is the id of the message to mark as read.
Returns a [Done Response](#doneresponse) in case of a success.
#### Delete a message
```http request
DELETE /messages/:messageID
```
messageID is the id of the message to delete.
Returns a [Done Response](#doneresponse) in case of a success.
#### Delete a message thread
```http request
DELETE /messages/threads/:threadID
```
threadID is the id of the thread to delete.
Returns a [Done Response](#doneresponse) in case of a success.
***
## Notifications
### Notification object
```properties
{
    id	string
    content	string
    dateCreation	date-time
    status	[NON_VU, VU]
    link	string
}
```
### Notification endpoints
#### Get the notifications for the current user
```http request
GET /notifications
```
Paginated, for query parameters see the [Pagination section](#pagination). Returns [notifications](#notification-object)
for the current user sorted by the date of last notifications.

#### Get the unread notifications for the current user
```http request
GET /notifications/unread
```
Paginated, for query parameters see the [Pagination section](#pagination). Returns the unread [notifications](#notification-object)
by the current user sorted by the date of last notifications.

#### Mark a notification as read
```http request
GET /notifications/:notificationID/read
```
notificationID is the id of the notification to mark as read.
Returns a [Done Response](#doneresponse) in case of a success.

#### Delete a notification
```http request
DELETE /notifications/:notificationID
```
notificationID is the id of the notification to delete.
Returns a [Done Response](#doneresponse) in case of a success.

#### Create a new notification
```http request
POST /notifications
```
Has no parameters. Returns the newly created [notification object](#notification-object). Requires the following request body format:

```json
{
    "userId": "o5eYbF3Pt7x9",
    "content": "My other other rook",
    "link": "http://site.interne.fr/lieninterne"
}
```
It should be noted that the lien is optional and can be omitted. userID is the ID of the user that the notification should be added to. Also this endpoint may get deprecated in the future.
***
## Security

### Signup

```http request
POST /security/signup
```
Create a new user (proprio or acquereur). Has no parameters. Requires the following request body format:

```json
{
    "firstName": "string",
    "lastName": "string",
    "email": "string",
    "password": "string",
    "phoneNumber": "string",
}
```
Returns a the following response containing the newly created userID.
```json
{
    "id": "string",
}
```
Can also return a validation error, Email is already in use error or a phone number already in use error.

### Signin

```http request
POST /security/signin
```
Logs in the user(regardless of the user role). Has no parameters.  Requires the following request body format:
```json
{
	"emailOrPhone": "string",
	"password": "string"
}
```
Returns a JWT in the following format.
```json
{
    "token": "string",
}
```
Can also return an validation error or an authentication failed error if either the password is wrong or the email,phone numbers don't exist in the database.

### Refresh Token
```http request
GET /security/refreshToken
```
***This is the only security endpoint that requires authentication***
Has no parameters.
Returns a refreshed JWT in the following format.

```json
{
    "token": "string",
}
```
Can also return an authentication failed error if either the token is expired or if the token doesn't match a user or if the token isn't valid.
### Mail confirmation

```http request
GET /security/mailconfirm/:confirmationCode
```
This is done by email so when the user clicks on the link in the email he is redirected to this path. When it is done he will be redirected to a login page(I'll need a link). Or an error page (I'll need a link) with the following errors: TokenExipred or TokenNotValid.

### Resend Mail confirmation
```http request
GET /security/mailconfirm/:userID/resend
```
This will resend an email to the user containing the link with a new token.
Return a [Done Response](#doneresponse) or a EntityNotFound error if the userID is incorrect.

### SMS confirmation
```http request
GET /security/smsconfirm/:confirmationCode
```
Confirmation code is the code sent by SMS. Activates the user account.
Returns a [Done Response](#doneresponse) if the code is correct. Or a TokenNotValid or a TokenExipred error if something is wrong.

### Password forgotten
```http request
POST /security/passwordforgetten
```
Has no parameters. Requires the following body format
```json
{
    "email": "string"
}
```
Returns a [Done Response](#doneresponse). Send an email linking to a page(should get an url for this) with a token in the parameters.

Will not return an error if the email is not associated with an account.
### Password forgotten
```http request
POST /security/resetpassword/:token
```
token is the token from the email. Requires the following request body format
```json
{
    "password": "string"
}
```
Returns a [Done Response](#doneresponse) in case of success. Or TokenNotValid or a TokenExipred error if something is wrong.
***
## Owner
## Owner Object
```properties
{
    id Text #this is the same id of the user object.
    firstName Text
    lastName Text
    email Text
    phoneNumber Text
    maritalStatus  Enum
        [+Célibataire
        +PACSÉ
        +MariéAvecContract
        +MariéSansContract
        +Divorcé
        +Veuf]
    civilite Enum
        [+Monsieur
        +Madame]
    adresse {
        Adresse: Text
        CodePostal: Number
        Ville: Text
        Pays: Text
    }
}
```
### Owner endpoints
#### Get the current logged in owner info
```http request
GET /owners/me
```
Has no parameters. Returns currently logged in [owner](#owner-object).

#### Get all the owners
```http request
GET /owners
```
Paginated, for query parameters see the Pagination section. Returns a paginated list of [owners](#owner-object).

#### Get an owner
```http request
GET /owners/:ownerId
```
OwnerID is the ID of the owner to fetch. Returns the specified [owner](#owner-object). Can also return an EntityNotFound Error.

#### Update the current logged in owner
```http request
PUT /owners/me
```
***This should be called after a user has signed up as an owner to fill the rest of the fields***
Updates the currently logged in owner. Has no parameters. Requires the [owner](#owner-object) while omitting the id field. Returns the updated [owner](#owner-object) resource.
Can
#### Update an owner
```http request
PUT /owners/:ownerId
```
Updates the specified owner. OwnerID is the ID of the owner to fetch. Requires the [owner](#owner-object) while omitting the id field. Returns the updated [owner](#owner-object) resource. Can also return an EntityNotFound error.
***
## Property
### Property Object
This is format is shared for all the property objects
```properties
{
    id Text
    idOwner Text
    type Enum
        [Maison,
        Appartement,
        Immeuble,
        Parking,
        Terrain,
        Commerce]
    isApproved boolean #optional
    surface Number
    description Text
    keyPoints   Text #optional
    photos[
        {
            id Text
            link Text
        }
    ]
    adresse {
        Adresse: Text
        CodePostal: Number
        Ville: Text
        Pays: Text
    }
    nbChambres Number #optional
    nbPieces Number #optional
    nbWC  Number #optional
    nbStationnements Number #optional
    nbEtages Number #optional
    anneeConstruction Number #optional
    surfaceHabitable Number #optional
}
```
#### Create a new Property
```http request
POST /properties
```
Has no parameters. Returns the newly created [Property](#property-object). Requires the body format of a property while **omitting** the ID, and photos. You can also omit any of the optional parameters.


#### Get all the properties

```http request
GET /properties
```
Paginated, for query parameters see the [Pagination section](#pagination). Returns [Property](#property-object).

#### Get all the properties by owner

```http request
GET /owners/:ownerId/properties
```
:ownerId is the id of the owner to list the properties of.
Paginated, for query parameters see the [Pagination section](#pagination). Returns [Property](#property-object).


#### Get all the properties followed by a buyer

```http request
GET /buyers/:buyerID/followedproperties
```
:buyerID is the id of the buyer to list the properties that he is intrested in.
Paginated, for query parameters see the [Pagination section](#pagination). Returns [Property](#property-object).

#### Get a specific property
```http request
GET /properties/:propID
```
**propID** is the ID of the property to consult.
Returns a [Property](#property-object) if it is successful or an EntityNotFound Error.

#### Modify property informations
```http request
PUT /properties/:propID
```
**propID** is the ID of the property to modify.

Requires the same body format as that of a [Create new Property](#create-a-new-property) request.
Returns the modified [Property](#property-object) if the request is successful or an EntityNotFound Error.

#### Delete a property
```http request
DELETE /properties/:propID
```
**propID** is the ID of the property to delete.
Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.


#### Set a property status to approved

```http request
GET /properties/:propID/approve
```
**propID** is the ID of the property to mark as approved.
Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.

#### Follow the changes of a specific property

```http request
GET /properties/:propID/follow
```
**propID** is the ID of the property to followed.
Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.

This will send notifications once a change in said property is detected.

#### Download a pdf recap of a property

```http request
GET /properties/:propID/follow
```
**propID** is the ID of the property to download.
Returns a PDF Recap if it is successful or an EntityNotFound Error.

***
## Disponibility
### Disponibility object
```properties
{
    id	string
    startTime date-time
    endTime	date-time
    isAllDay boolean
}
```


### Disponibility endpoints

#### Create a new disponibility
```http request
POST /disponibility/create
```

Has no parameters. Returns the newly created [Disponibility object](#Disponibility-object). Requires the following request body format:

```json
{   "id": "string",
    "startTime": "string",
    "endTime": "string"
}
```
#### Delete disponibility
```http request
DELETE /disponibility/:disponibilityId
```
disponibilityID is the id of the disponibility to delete.
Returns a [Done Response](#doneresponse) in case of a success.
#### Update disponibility
```http request
PUT /disponibility/:disponibilityId
```
Updates the specified disponibility. disponibilityId is the ID of the disponibility to fetch. Requires the [disponibility](#disponibility-object) while omitting the id field. Returns the updated [disponibility](#disponibility-object) resource. Can also return an EntityNotFound error.


### user-Disponibility object
```properties
{
    id	string
    disponibilityId string
    status Enum
        [PENDING,
        REFUSED,
        ACCEPTED]
    prestataireId string
    userId string #optional
    titre string #optional
    typeOfTask string #optional
    frequencyOfTask string #optional
    notificationTime date-time #optional


}
```
### user-Disponibility endpoints

#### Assign disponibility to User
```http request
PUT /user-disponibility/assign/:disponibilityId/:userId
```
disponibilityId is the Id of disponibility that the user will chose to assign
userId is the id of the user that will assign that disponibility
 Returns a [Done Response](#doneresponse) in case of a success.

#### delete user disponibility
```http request
DELETE /user-disponibility/:disponibilityId/:userId
```

disponibilityId is the Id of disponibility that the user will chose to delete
userId is the id of the user that will delete that disponibility
 Returns a [Done Response](#doneresponse) in case of a success.
 ####  Update user disponibility
```http request
PUT /user-disponibility/:disponibilityId/:userId
```

disponibilityId is the Id of disponibility that the user will chose to divest
userId is the id of the user that will divest that disponibility
 Returns a [Done Response](#doneresponse) in case of a success.

 #### Request meeting

 ```http request
PUT /user-disponibility/requestMeeting/:userdisponibilityId
```
userdisponibilityId is the Id of userdisponibilityId that the user will chose to request a meeting

Has no parameters. Returns the newly created [user-disponibility object](#user-disponibility-object)Requires the following request body format:

```json
{
    "titre": "string",
    "typeOfTask": "string",
    "frequencyOfTask" :"string",
    "notificationTime":"string"
}
```
#### cancel meeting request
 ```http request
PUT /user-disponibility/cancellRequestMeeting/:userdisponibilityId
```
userdisponibilityId is the Id of userdisponibilityId that the user will chose to request a meeting
Returns a [Done Response](#doneresponse) in case of a success.

#### Accept meeting request

 ```http request
PUT /user-disponibility/acceptMeetingRequest/:userdisponibilityId
```
userdisponibilityId is the Id of userdisponibilityId that the user will chose to confirm a meeting
Returns the newly updated [user-disponibility object](#user-disponibility-object).

#### Refuse meeting request

 ```http request
PUT /user-disponibility/refuseMeetingRequest/:userdisponibilityId
```
userdisponibilityId is the Id of userdisponibilityId that the user will refuse a meeting
Returns the newly updated [user-disponibility object](#user-disponibility-object).
***
## Visit Reviews
### VisitReview Object
This is format is shared for all the visit reviews objects
```properties
{
    id Text
    idVisit Text
    comment Text
    intrestLevel Enum
        [
            WillingToBuy
            WantToRevist
            Intrested
            NotInrested
        ]
    created_at Timestamp
}
```
### VisitReview Endpoints
#### Create a new Review
```http request
POST /visitreviews
```
Has no parameters. Returns the newly created [review](#visitreview-object). Requires the body format of a property while **omitting** the ID, and the created_at.

#### View the reviews by property
```http request
GET /properties/:propID/visitreviews
```
**propID** is the ID of the property to consult the reviews of.
Paginated, for query parameters see the [Pagination section](#pagination).
Returns [reviews](#visitreview-object) if it is successful or an EntityNotFound Error.

***
## Offers
### Offer Object
```properties
{
    id Text
    idProperty Text
    idBuyer Text
    idPreviousOffer Text #optional
    created_date DateTime
    end_date DateTime #optional
    price Number
    status Enum
        [
            accepted
            refused
        ]

    #commission stuff really isn' clear yet.
}
```
#### Create a new offer
```http request
POST /properties/:propID/makeoffer
```
Has no parameters. Returns the newly created [offer](#offer-object). Requires following body format.
```properties
{
    idPreviousOffer #optional
    end_date DateTime #optional
    price Number
}
```
#### Consult offers for the current owner
```http request
GET /offers/me
```
Paginated, for query parameters see the [Pagination section](#pagination). Returns [offers](#offer-object)
for the current user sorted by the date of last offer.

#### Consult offers for a specific owner
```http request
GET /owners/:ownerId/offers
```
**ownerID** is the ID of the owner to fetch the offers he has.
Paginated, for query parameters see the [Pagination section](#pagination).
Returns [offers](#offer-object) recived by a specific owner. Can also return an EntityNotFound Error.

#### Consult offers for a specific property
```http request
GET /properties/:propID/offers
```
**propID** is the ID of the property to fetch the offers it recived.
Paginated, for query parameters see the [Pagination section](#pagination).
Returns [offers](#offer-object) for a specific property. Can also return an EntityNotFound Error.

#### Get the counteroffers of an offer

```http request
GET /offers/:offerID/counteroffers
```
**offerID** is the ID of the offer to fetch the counteroffers it recived.

[Pagination section](#pagination).
Returns [counteroffers](#offer-object) for a specific offer. Can also return an EntityNotFound Error.

#### Accept an offer
```http request
GET /offers/:offerID/approve
```
**offerID** is the ID of the offer to mark as accepted.
Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.

#### Refuse an offer
```http request
GET /offers/:offerID/refuse
```
**offerID** is the ID of the offer to mark as refused.
Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.
***

## adverts

### adverts Object
```properties
{
    id string
    proprietaireId string
    title string
    generalInfo string
    detailedInfo string
    rating string ##optional
    InterrestedUsers Number ##optional
    chaufage string
    advertVideo string ##optional
    country string
    city string
    latitude string
    longitude string
    properties Properties # [Property](#property-object)
    created_at Date-time

}
```
#### create-a-new-advert
```http request
POST /adverts
```
Has no parameters. Returns the newly created [adverts](#adverts-object). Requires the body format of a property while **omitting** .

```properties
{
    title string
    generalInfo string
    detailedInfo string
    chaufage string
}
```
#### Modify advert
```http request
PUT /adverts/:advertID
```
**advertID** is the ID of the advert to modify.

Requires the same body format as that of a [create-a-new-advert](#create-a-new-advert) request.
Returns the modified [Adverts Object](#adverts-object) if the request is successful or an EntityNotFound Error.

#### Delete advert
```http request
DELETE /adverts/:advertID
```
**advertID** is the ID of the advert to delete.
Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.

#### Get all adverts

```http request
GET /adverts
```
return a paginated list of [Adverts Object](#adverts-object) 

#### Get my adverts

```http request
GET /adverts/:ownerId
```
propritetaireId :is the id of the owner that had created that advert .
return [Adverts Object](#adverts-object) if the request is successful or an EntityNotFound Error.
#### Get a specific advert

```http request
GET /adverts/:advertID
```
advertID :is the id of the advert to get a specific advert.
return [Adverts Object](#adverts-object) if the request is successful or an EntityNotFound Error.
#### Get advert by type

```http request
GET /adverts/:country
```

 **country**:string : is the country that the user will search to find adverts
return [Adverts Object](#adverts-object) if the request is successful or an EntityNotFound Error.

#### Set advert interested
```http request
PUT /adverts/:advertID/interested
```
**advertID** is the ID of the advert to mark as interested.
Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.

## visits
### visits Object
```visits
{
    id string
    idOwner string
    idPurchaser string
    visitTitle string
    message string
    typeOfVisit Enum
        [
            + virtual 
            +  physical
        ]
    heuresChoisit string #optional 
    dateChoisit string #optional
    rappel boolean #optional
    hasValidProfile boolean 
    visioUrl string #optional
    status Enum
        [PENDING,
        REFUSED,
        ACCEPTED]
    purchaseFiles string #optional
    suggestedTime string
    created_date DateTime
}
```

#### Create a new virtual visit
```http request
POST /visits/virtual
```
Has no parameters. Returns the newly created  virtual [visits](#visits-object). Requires the body format of a property while **omitting** the ID,. You can also omit any of the optional parameters.

#### Create a new physical visit
```http request
POST /visits/physical
```
Has no parameters. Returns the newly created  physical [visits](#visits-object). Requires the body format of a property while **omitting** the ID,. You can also omit any of the optional parameters.


#### modify a visit
```http request
PUT /visits/:visitId
```
**visitId** is the ID of the visit to modify.
Requires the same body format as that of a [Create a new virtual visit](#create-a-new-virtual-visit) request.
Returns the modified [visits](#visits-object) if the request is successful or an EntityNotFound Error.
#### Delete a visit
```http request
DELETE /visits/:visitId
```
visitId is the id of the visit to delete.
Returns a [Done Response](#doneresponse) in case of a success.
 #### Get all visits
 ```http request
GET /visits
```

return a paginated list of [visits](#visits-object)

#### Get specific visit

```http request
GET /visits/:visitId
```
visitId :is the id of the visit to get a specific visit.
returns [visits Object](#visits-object) if the request is successful or an EntityNotFound Error.

#### Get specific type of visits
```http request
GET /visits/:visitType
```
**visitType** :is the type of the visit to get a specific visit.
**visitType** may be either [virtual] or [physical]
return [visits Object](#visits-object) if the request is successful or an EntityNotFound Error.

#### Get pending visits
```http request
GET /visits/pending
```
Has no parameters.
returns [visits Object](#visits-object) 
#### Approve pending visits
```http request
PUT /visits/:visitId/approve
```
**visitId** is the ID of the visit to mark as approved.
Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.
#### Reject pending visits
```http request
PUT /visits/:visitId/reject
```
**visitId** is the ID of the visit to mark as refused.
Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.

#### Approve and suggest another timing
```http request
PUT /visits/:visitId/:suggestedTime/approveAndSuggest
```
**visitId** is the ID of the visit to mark as approved 
**suggestedTime** is the time that the owner will suggest  to visit

Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.


## services
### services Object
```services
{
    id string
    title string
    description string
    picture string #optional 
    serviceConditions string
    created_date DateTime
}
```
#### Create a new service

```http request
POST /services
```
Has no parameters. Returns the newly created  [services](#services-object). Requires the body format of a service while **omitting** the ID,. You can also omit any of the optional parameters.
#### modify a service
```http request
PUT /services/:serviceId
```
**serviceId** is the ID of the service to modify.
#### Delete a service 
```http request
DELETE /services/:serviceId
```
**serviceId** is the ID of the service  to delete.
Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.

#### Get all services
 ```http request
GET /services
```

return a paginated list of [services](#services-object)
## service-request
#### service request Object
```service-request
{
    id string
    serviceId string
    ownerId string
    termsAccepted boolean
    paymentType Enum [
        +BANK_ACCOUNT
        +BANK_TRANSFER
    ]
    status Enum
        [Pending,
        Rejected,
        Acccepted]
    comments String
    files String
    created_date DateTime
}
```
#### request a service

```http request
POST /service-request/
```
Has no parameters. Returns the newly created  [service request Object](#service-request-object). Requires the body format of a property while **omitting** the ID,. You can also omit any of the optional parameters.

#### Approve a service request

```http request
PUT /service-request/:serviceRequestId/approve
```
**serviceRequestId** is the ID of the service request to mark as approved 

Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.

#### Reject a service request
```http request
PUT /service-request/:serviceRequestId/refuse
```
**serviceRequestId** is the ID of the service request to mark as refused 

Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.
#### modify a service request

```http request
PUT /service-request/:serviceRequestId
```
**serviceRequestId** is the ID of the service request to modify.

Requires the same body format as that of a [request-a-service](#request-a-service) request.
Returns the modified [service-request](#ervices-request-object) if the request is successful or an EntityNotFound Error.

#### Delete a service request
```http request
DELETE /service-request/:serviceRequestId
```
**serviceRequestId** is the ID of the service request to delete.

Returns a [Done Response](#doneresponse) in case of a success.

#### Get a specific service request
```http request
GET /service-request/:serviceRequestId
```
**propID** is the ID of the property to consult.
Returns a [service-request](#service-request-object) if it is successful or an EntityNotFound Error.

#### Get all service request

```http request
GET /service-request
```
return [service request Object](#services-request-object) if the request is successful or an EntityNotFound Error.

#### Get pending service request

```http request
GET /service-request/pending
```
return all pending [service request Object](#services-request-object) if the request is successful or an EntityNotFound Error.


## rating
### rating Object

```rating
{
    id string
    userId string
    orderId string
    rating double
    comment string
    created_date DateTime
}
```
#### create a new rating
```http request
POST /rating
```
Has no parameters. Returns the newly created [rating](#rating-object). Requires the body format of a property while **omitting** the ID. You can also omit any of the optional parameters.

#### modify a rating
```http request
PUT /rating/:ratingId
```
**ratingId** is the ID of the rating request to modify.

Requires the same body format as that of a [rating](#rating) request.
Returns the modified [rating](#rating-object) if the request is successful or an EntityNotFound Error.

#### Delete a rating
```http request
DELETE /rating/:ratingId
```
**ratingId** is the id of the rating to delete.
Returns a [Done Response](#doneresponse) in case of a success.

#### Get all rating
 ```http request
GET /rating
```
return a paginated list of [rating](#rating-object)
#### Get a specific rating

```http request
GET /rating/:ratingId
```
**ratingId**  :is the id of the rating to get a specific rating.
returns [rating Object](#rating-object) if the request is successful or an EntityNotFound Error.


## refund request
### refund request Object

```properties
{
    id	string
    userId string
    serviceId string
    title string
    message string
    documents string
    isApproved boolean
    isChecked boolean 
    date Date   
}
```
#### Create a new refund request
```http request
POST /refund-service/
```
Has no parameters. Returns the newly created [refund](#visits-object). Requires the body format of a property while **omitting** the ID,. You can also omit any of the optional parameters.



#### Get all refund request

```http request
GET /refund-service/
```
return a paginated list of [refund request Object](#refund-request-object)


#### Get a specific refund request 

```http request
GET /refund-service/serviceId
```
serviceId :is the id of the service to get a specific refund request.
return [refund request Object](#refund-request-object) if the request is successful or an EntityNotFound Error.

#### Approve a refund request

```http request
PUT /refund-service/serviceId/approve
```
**serviceId** is the ID of the refund request to mark as approved 

Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.

#### Reject a refund request
```http request
PUT /refund-service/serviceId/refuse
```
**serviceId** is the ID of the refund request to mark as refused 

Returns a [Done Response](#doneresponse) if it is successful or an EntityNotFound Error.