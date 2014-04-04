require 'faker'

def gen(number, &block)
  n = if number.is_a?(Range)
        number.last < Float::INFINITY ? number.last : 9
      else
        number
      end
  min = number.is_a?(Range) ? number.first : 0

  res = (rand(n + 1) + min).times.map(&block).compact
  res.empty? ? nil : res
end

def gen_identifier(number = 3)
  tpls = [
    { use: 'official', label: 'BSN', system: 'urn:oid:2.16.840.1.113883.2.4.6.3', value: '123123' },
    { use: "official", label:"SSN", system:"urn:oid:2.16.840.1.113883.2.4.6.7", value:"123456789" }]

  gen(number) do
    tpls.sample.merge value: (1000000 + rand(1000000)).to_s, use: %w(usual  official  temp  secondary).sample
  end
end

def gen_active(number = 1..1)
  [true, false].sample
end

def gen_codeable_concept(number = 1..1)
  res = {}
  # object :coding, (0..Float::INFINITY) do
  #   # Coding Code defined by a terminology system
  # end
  gen(0..1) do
    res[:text] = Faker::Lorem.sentence # Plain text representation of the concept
  end

  res unless res.empty?
end

def gen_category(number = 1..1)
  gen(3) do
    { scheme: Faker::Internet.url('http://hl7.org/fhir/tag/'), term: Faker::Internet.url, label: Faker::Lorem.sentence }
  end
end

def gen_HumanName(number = 1..1)
  gen(number) do
    {
      use: %w(usual  official  temp  nickname  anonymous  old  maiden).sample,
      text: Faker::Name.name,
      family: gen(2) { Faker::Name.last_name },
      given: gen(2) { Faker::Name.first_name },
      prefix: gen(1) { Faker::Name.prefix }
    }
  end
end
alias :gen_name :gen_HumanName

def gen_telecom(number = 1..1)
  gen(2) do
    [
      {
        system: %w(phone fax).sample,
        value: Faker::PhoneNumber.cell_phone,
        use: %w(home  work  temp  old  mobile).sample
      },
      {
        system: 'email',
        value: Faker::Internet.email,
        use: %w(home  work  temp  old  mobile).sample
      }
    ].sample
  end
end

def gen_gender(number)
  gen(1) do
    {
      coding: [{ system: 'http://hl7.org/fhir/v3/AdministrativeGender', code: %w(M F).sample, display: %w(Male Female).sample }] # bug
    }
  end
end

def gen_birthDate(number = 1..1)
  Time.at(rand * Time.now.to_i)
end

def gen_deceasedBoolean(number = 1..1)
  rand % 100 == 0
end

def gen_address(number = 2)
  gen(number) do
    res = { use: %w(home work temp old).sample,
      line: [Faker::Address.street_address],
      city: Faker::Address.city,
      zip: Faker::Address.zip,
      state: Faker::Address.state
    }
    res[:text] = [res[:line].join("\n"), res[:state], res[:zip]].join(", ")
    res
  end
end

def gen_contact(number = 1..1)
  gen(number) do
    res = {}

    res[:system] = gen_code(0..1, restrictions: %w(phone fax email url))
    res[:value] = gen(0..1) { Faker::Lorem.sentence } # The actual contact details
    res[:use] = gen_code(0..1, restrictions: %w(home work temp old mobile)) # purpose of this address
    res[:period] = gen_period(0..1) # Period Time period when the contact was/is in use

    res.delete_if { |_, v| !v }
    res unless res.empty?
  end
end

def gen_period(number = 1..1)
  datetime_min = Time.new(2000)
  datetime_max = Time.new(2010)
  rand_datetime = -> do
    Time.at((datetime_max.to_f - datetime_min.to_f) * rand + datetime_min.to_f)
  end

  gen(number) do
    res = {}

    res[:start] = gen(0..1) { rand_datetime.call } # Starting time with inclusive boundary
    res[:end]   = gen(0..1) { rand_datetime.call } # End time with inclusive boundary, if not ongoing

    res.delete_if { |_, v| !v }
    res unless res.empty?
  end
end

def gen_code(number = 1, opts = {})
  restrictions = opts[:restrictions]
  if !restrictions.is_a?(Array)
    restrictions = [restrictions]
  end
  if restrictions.empty?
    restrictions = Faker::Lorem.words
  end

  gen(number) do
    restrictions.sample
  end
end

def gen_location(number = 1..1)
  gen(number) do
    res = {}
    res[:identifier] = gen_identifier(0..1) # Unique code or number identifying the location to its users
    res[:name] = gen(0..1) { Faker::Lorem.word } # Name of the location as used by humans
    res[:description] = gen(0..1) { Faker::Lorem.paragraphs } # Description of the Location, which helps in finding or referencing the place

    res.delete_if { |_, v| !v }
    res unless res.empty?
  end
end

def gen_maritalStatus(number = 1..1) end
def gen_photo(number = 1..1) end
def gen_communication(number = 1..1) end
def gen_careProvider(number = 1..1) end

def gen_organization(number = 1..1)
  gen(number) do
    res = {}
    res[:identifier] = gen_identifier(0..Float::INFINITY) # Identifier Identifies this organization  across multiple systems
    res[:name] = gen(0..1) { Faker::Company.name } # Name used for the organization
    res[:type] = gen_codeable_concept(0..1) # Kind of organization
    res[:telecom] = gen_contact(0..Float::INFINITY) # Contact A contact detail for the organization
    res[:address] = gen_address(0..Float::INFINITY) # Address An address for the organization </address>
    res[:partOf] = gen_organization(0..1) # The organization of which this organization forms a part
    res[:contact] = gen(0..Float::INFINITY) do # Contact for the organization for a certain purpose
      contact = {}
      contact[:purpose] = gen_codeable_concept(0..1) # The type of contact
      contact[:name] = gen_HumanName(0..1) # A name associated with the contact
      contact[:telecom] = gen_contact(0..Float::INFINITY) # Contact details (telephone, email, etc)  for a contact
      contact[:address] = gen_address(0..1) # Visiting or postal addresses for the contact
      contact[:gender] = gen_codeable_concept(0..1) # Gender for administrative purposes
      contact.delete_if { |_, v| !v }
      contact unless contact.empty?
    end
     res[:location] = gen_location(0..Float::INFINITY) # Location(s) the organization uses to provide services
     res[:active] = gen_active(0..1) # Whether the organization's record is still in active use

    res.delete_if { |_, v| !v }
    res unless res.empty?
  end
end
alias :gen_managingOrganization :gen_organization

def gen_patient(number)
  gen(number) do
    res = { resourceType: 'Patient' }

    {
      identifier:           0..Float::INFINITY,
      category:             0, #wtf?
      name:                 0..Float::INFINITY,
      telecom:              0..Float::INFINITY,
      gender:               0..1,
      birthDate:            0..1,
      deceasedBoolean:      0..1,
      address:              0..Float::INFINITY,
      maritalStatus:        0..1,
      photo:                0..Float::INFINITY,
      contact:              0..Float::INFINITY,
      communication:        0..Float::INFINITY,
      careProvider:         0..Float::INFINITY,
      managingOrganization: 0..1,
      active:               0..1
    }.each do |name, number|
      res[name] = send("gen_#{name}", number)
      res.delete_if { |k, v| v.nil? }
    end

    res
  end
end
