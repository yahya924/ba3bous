Hey :) I'm using json-generator, check: https://json-generator.com

````text
[
  '{{repeat(5, 10)}}',
  {
    id: '{{index()+152}}',
    amount: '{{integer(50, 300)}}',
    amountPaid: function () {
      return this.amount - 50;
    },
    titular:'{{firstName()}} {{surname()}}',
    creationDate:'{{date(new Date(2020, 0, 1), new Date(), "YYYY-MM-ddThh:mm:ss")}}',
    paymentDate: '{{date(new Date(2020, 1, 1), new Date(), "YYYY-MM-ddThh:mm:ss")}}',
    status: function(){
      
      var reference = Math.floor(Math.random() * 3);
      var result;
      
      switch(reference) {
        case 0:
            result = "PD";
            break;

        case 1:
            result = "PN";
            break;

        case 2:
            result = "IP";
            break;
      }
      
      return result;
      
  	},
    hadMissingDocs: '{{bool()}}'
   
  }
]
````