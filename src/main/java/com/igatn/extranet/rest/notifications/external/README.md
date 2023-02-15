Hey :) I'm using json-generator, check: https://json-generator.com

```json
[
  "{{repeat(4, 5)}}",
  {
    "id": "{{index() + 54}}",
    "title": "{{lorem(1, \"sentences\").substr(0,10)}}",
    "description": "{{lorem(1, \"sentences\").substr(0,40)}}",
    "creationDate": "{{date(new Date(2022, 7, 1), new Date(), \"YYYY-MM-ddThh:mm:ss Z\")}}",
    "type": "{{random(\"reimbursement\", \"exchange\", \"payment\")}}",
    "seen": "{{random(true, false)}}"
  }
]
```