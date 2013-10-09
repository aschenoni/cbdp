class TweetReader implements Runnable {
	public void run() {
		try {
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} 
	}
}
