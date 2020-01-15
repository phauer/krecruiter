FROM python:3.8.1-alpine3.11

RUN pip install --ignore-installed "flask==1.1.1" "faker==4.0.0"

COPY address-validation-service.py /
CMD python3 /address-validation-service.py