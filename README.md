SharedEventBus
==============

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

