FROM gradle:8.10.2-jdk21
WORKDIR /app
COPY . /app
CMD ["gradle", "bootRun", "--no-daemon"]
