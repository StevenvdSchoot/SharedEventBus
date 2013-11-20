SharedEventBus
==============

About
-----

The goal of this library is to provide a reliable event bus that can be shared between multiple application. It can be shared between application running on the same machine as well as applications running on other machines.

One of the features shared event bus is providing the ability to share events to everyone connected to the event bus as well as a small group of applications. To ensure an event is only readable for applications part of the group authorization using a combination of a group name and a group password is required before being able to send or receive events target for a specific group. One application can be part of multiple groups at the same time.

API
---

To create a new eventbus object use:
```java
InetSocketAddress[] inetSocketAddressList = new InetSocketAddress[]{
	new InetSocketAddress("localhost", 8081)
};
SharedEventBus eventBus = new SharedEventBus(inetSocketAddressList);
```

To handle an event, you need to create an event handler:
```java
class someEvent {
	// ...
}
class eventHandler {
	@Subscribe
	void onReceivedSomeEvent(someEvent event) {
		// ...
	}

	@Subscribe
	void onReceivedAString(String string) {
		// ...
	}
}
```

Then you can asign the event handler to the event bus using:
```java
eventBus.addHandler(new eventHandler());
```

You can now fire events on the event bus using:
```java
eventBus.fire(new someEvent());
eventBus.fire("this is a test event");
```

To use secure events you need to subscribe to a group first:
```java
eventBus.addGroup("group name", "secret password");
```

After subscribing to a group you will emediatly receive events targeted for this group. To send an event withing the group use:
```java
eventBus.fire( new SecureEvent( "group name", "a test event" ) );
eventBus.fire( new SecureEvent( "group name", new someEvent() ) );
```

