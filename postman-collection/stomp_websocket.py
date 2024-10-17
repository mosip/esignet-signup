import asyncio
import websockets
import sys
import uuid

# Function to construct the WebSocket connection URL with slotId as a query parameter
def construct_ws_url(base_url, slot_id):
    return f"{base_url}?slotId={slot_id}"

# Function to send messages or disconnect based on user input
async def handle_user_input(websocket):
    while True:
        choice = input("Do you want to send a message or disconnect? (Type 'send' to send, 'disconnect' to disconnect): ").strip().lower()

        if choice == 'send':
            message = input("Enter the message to send: ")
            if message:
                # Construct the SEND frame to the specific destination
                send_frame = f"SEND\ndestination:/v1/signup/ws/process-frame\ncontent-type:application/json\n\n{message}\x00"
                await websocket.send(send_frame)
                print(f"Message sent: {send_frame}")
            else:
                print("No message entered. Skipping send.")

        elif choice == 'disconnect':
            print("Disconnecting from the server...")
            await websocket.close()
            break

        else:
            print("Invalid option. Please type 'send' or 'disconnect'.")

# Function to receive messages from the server
async def receive_message(websocket):
    while True:
        try:
            response = await websocket.recv()
            print(f"Message received from server: {response}")
        except websockets.exceptions.ConnectionClosed:
            print("Connection closed by the server.")
            break
        except Exception as e:
            print(f"Error receiving message: {e}")
            break

async def connect_to_websocket(base_url, slot_id, cookie):
    uri = construct_ws_url(base_url, slot_id)

    # Define headers with the cookie
    headers = {
        'Cookie': f'{cookie}'
    }

    # Connect to WebSocket using the provided URI and headers
    async with websockets.connect(uri, extra_headers=headers) as websocket:
        print(f"Connected to WebSocket at {uri}")

        # Generate a unique subscription ID
        subscription_id = str(uuid.uuid4())

        # Subscribe to the /topic/slotId destination
        subscribe_frame = f"SUBSCRIBE\nid:{subscription_id}\ndestination:/topic/{slot_id}\n\n\x00"
        await websocket.send(subscribe_frame)
        print(f"{subscribe_frame}")

        # Create two tasks: one for sending messages or disconnecting, and another for receiving messages
        receive_task = asyncio.create_task(receive_message(websocket))
        send_task = asyncio.create_task(handle_user_input(websocket))

        # Run both tasks concurrently
        await asyncio.gather(receive_task, send_task)

# Entry point: Get baseUrl, slotId, and cookie from user input
if __name__ == "__main__":
    if len(sys.argv) != 4:
        print("Usage: python stomp_websocket_send_or_disconnect.py <baseUrl> <slotId> <cookie>")
        sys.exit(1)

    base_url = sys.argv[1]
    slot_id = sys.argv[2]
    cookie = sys.argv[3]

    # Start the WebSocket connection
    asyncio.get_event_loop().run_until_complete(connect_to_websocket(base_url, slot_id, cookie))
