FROM docker.io/library/openjdk:15-alpine

RUN mkdir /wach
ADD build/distributions/app.tar /wach

WORKDIR "/wach"
CMD ["app/bin/app"]
