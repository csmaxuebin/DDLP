function [z,z_true,MAX,time_elps,count,euc_dist] = exp_mechanism(true_loc, state_no, eps, DeltaX, T)
%MAX must shuchu
tic;
count=0;
euc_dist=0;
%check if true_data is in the possible set 'DeltaX'
possible_set=sum(DeltaX,1);
if(possible_set(state_no)==0)% true_data is not included;
    minDist=100000000000;
    for tmp=1:size(DeltaX,1)
        if(norm(T*true_loc'-T*(DeltaX(tmp,:))')<minDist)
           z_true=T*DeltaX(tmp,:)';
        end
    end
else% true_data is included;
    z_true=T*true_loc';
end
%z_true=T*true_data';

%%%compute drift ratio  before disturb
possible_set=sum(DeltaX,1);
count=0;
%drift_ratio=0;
%size_deltax=size(DeltaX,1);
if(possible_set(state_no)==0)% true_data is not included;
   count+=1;
   %drift_ratio=count/size_deltax;
end

[MAX]=getSensitivity(T,DeltaX,z_true);
[scores]=getScore(T,true_loc);
if(MAX==0)
   z=z_true;
   time_elps=toc;
   return;
end

probabilities=[];
for tmp=1:size(scores,2)
  p=exp(-eps*scores(tmp)/(2*MAX));
  probabilities(tmp)=p;
end
%display(probabilities);

 pro=[];
 for tmp=1:size(probabilities,2)
 pro(tmp)=probabilities(tmp)/sum(probabilities);
end
%display(pro);
n=rand(1);
index=0;
for i=1:size(pro,2)
  n=n-pro(:,i);
  if(n<0)
    break
  end
  index=index+1;
end
if(index==0)
   z=T(:,index+1);
else
   z=T(:,index);
end
time_elps=toc;

%%%compute dist
dist=zeros(2,1);
dist=(z-(T*true_loc')).^2
euc_dist=sqrt(sum(dist(:)));


end




function [scores]=getScore(T,true_loc)
  %N=size(T,2)
  %scores=zeros(1,N)

  scores=[];
  for tmp=1:size(T,2)
      a=norm(T(:,tmp)-T*true_loc');   %l2 borm
      scores(tmp)=a;
   end
   %display(scores);
end

%function [MAX] = getSensitivity(T,DeltaX,z_true)

   %MAX=-inf;
   %for tmp=1:size(DeltaX,1)
        %%norm_l1=norm((T*true_loc'-T*(DeltaX(tmp,:))'),1)
        %norm_l1=norm((z_true-T*(DeltaX(tmp,:))'),1);
       % if(norm_l1>MAX)
          % MAX=norm_l1;
        %end
  % end
%end

function [MAX] = getSensitivity(T,DeltaX,z_true)

   MAX=-inf;
   Delta=T*DeltaX';
   for i=1:size(Delta,2)
       for j=1:size(Delta,2)
          %if(i==j)
             %continue;
           %end
          tmp=norm((Delta(:,i)-Delta(:,j)),2) %change
         if(tmp>MAX)
           MAX=tmp;
         end
     end
   end
end