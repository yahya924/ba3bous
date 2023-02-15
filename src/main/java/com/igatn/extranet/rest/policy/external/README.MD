Hey :) I'm using json-generator, check: https://json-generator.com

````text
[
  '{{repeat(1, 3)}}',
  {
    id: '{{objectId()}}',
    risk: '{{random("Santé","Autre")}}',
   	startDate: '{{date(new Date(2021, 11, 1), new Date(2022, 0, 1), "YYYY-MM-dd")}}',
    endDate: '{{date(new Date(2022, 11, 1), new Date(2023, 0, 1), "YYYY-MM-dd")}}',
    currency: 'EUR',
    beneficiaries: [
      '{{repeat(1, 3)}}',
      {
   		id: '{{objectId()}}',
        firstName:'{{firstName()}}',
        lastName:'{{surname()}}',
        relation:"Enfant",
        NSS: '{{integer(11111111111, 99999999999)}}',
        birthDate:'{{date(new Date(2010, 0, 1), new Date(2021,0,1), "YYYY-MM-dd")}}'
      }
    ],
    sumBeneficiaries: function(){ return this.beneficiaries.length;},
    bankAccounts:[
      '{{repeat(1,2)}}',
      {
        link: '{{random(["P"],["C"])}}',
        paymentMode: '{{random("Virement","Chèque")}}',
        iban: '{{integer(1111_1111_1111_1111, 9999_9999_9999_9999)}}',
        currency: '{{random("EUR","USD")}}'
      }
    ],
    guarantees:[
      '{{repeat(1,5)}}',
      {
   		id: '{{objectId()}}',
        name: '{{lorem(3, "words")}}'
      }
    ],
     documents:[
      '{{repeat(1,5)}}',
      {
   		id: '{{objectId()}}',
        name: '{{lorem(3, "words")}}',
        url: ''
      }
    ]
  }
]
````