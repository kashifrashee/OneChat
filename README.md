# OneChat

OneChat is a simple real-time chat application built using **Jetpack Compose** and **Firebase**. It allows users to register, log in, and send messages in real time.

## Features

- **User Authentication**: Sign up and log in using Firebase Authentication.
- **Real-Time Chat**: Send and receive messages instantly with Firestore.
- **User List**: View a list of registered users and start a conversation.
- **Unread Message Indicator**: Highlight unread messages in the user list.
- **Pull-to-Refresh**: Refresh the user list by pulling down.

## Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **Firebase Authentication**
- **Firestore (NoSQL Database)**

## Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/kashifrashee/OneChat.git
   ```
2. Open the project in **Android Studio**.
3. Connect your project to **Firebase**:
    - Add the `google-services.json` file to `app/` directory.
    - Enable Firestore and Authentication in the Firebase console.
4. Build and run the project on an emulator or physical device.

## How It Works

### User Authentication
- Users can **sign up** using an email and password.
- After signing up, users are redirected to the chat screen.
- Logged-in users see a list of all registered users (except themselves).

### Chatting
- Click on a user to start a conversation.
- Messages are sent and stored in **Firestore**.
- The most recent chats appear at the top.

### Unread Message Indicator
- If a user receives a message they haven't read yet, the chat will be highlighted in the user list.
- Once they open the chat, the messages are marked as read.

## Roadmap

- Add image sharing support
- Improve UI and animations
- Implement message deletion
- Enhance performance with pagination

## License

This project is licensed under the **MIT License**.

