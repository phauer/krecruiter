FROM python:3.7.2-alpine3.8

RUN pip install --ignore-installed "flask==1.0.2" "Faker==1.0.2"

COPY address-validation-service.py /
CMD python3 /address-validation-service.py