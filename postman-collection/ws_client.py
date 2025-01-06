import websocket
import threading
import sys

base_url=input("Enter the base URL (ws://localhost:8089/v1/signup/ws): ")
slot_id=input("Enter the slotId: ")
cookie=input("Enter the cookie value: ")

def on_message(ws, message):
    print("===================")
    print(f"Received {message}")
    print("===================")

def on_error(ws, error):
    print("Error:", error)

def on_close(ws, close_status_code, close_msg):
    print("Connection closed:", close_status_code, close_msg)

def on_open(ws):
    # Send STOMP CONNECT frame
    connect_frame = "CONNECT\naccept-version:1.2\n\n\x00"
    ws.send(connect_frame)
    print(f"{connect_frame}")

    # Subscribe to the /topic/slotId destination
    subscribe_frame = f"SUBSCRIBE\nid:sub-0\ndestination:/topic/{slot_id}\n\n\x00"
    ws.send(subscribe_frame)
    print(f"{subscribe_frame}")

    # Start a new thread to take user input and send messages to the WebSocket
    threading.Thread(target=send_user_input, args=(ws,)).start()

def send_user_input(ws):
    try:
        while True:
            user_input = input("Enter a message to send: ")
            if user_input.lower() == "exit":
                print("Closing connection...")
                ws.close()
                break

            # Send user input as a message to the WebSocket
            send_frame = f"SEND\ndestination:/v1/signup/ws/process-frame\ncontent-type:application/json\n\n{user_input}\x00"
            ws.send(send_frame)
            print(f"{send_frame}")

    except Exception as e:
        print("Error sending message:", e)

# WebSocket connection
def start_ws_client():
    uri = f"{base_url}?slotId={slot_id}"  # Replace with your WebSocket server's URI
    headers = {"Cookie": f"IDV_SLOT_ALLOTTED={cookie}"}  # Replace with any necessary headers

    ws = websocket.WebSocketApp(
        uri,
        header=headers,
        on_open=on_open,
        on_message=on_message,
        on_error=on_error,
        on_close=on_close
    )

    # Run the WebSocket with a blocking loop
    ws.run_forever()

# Run the subscribe function
if __name__ == "__main__":
    start_ws_client()
